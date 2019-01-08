package xyz.vopen.oss.gateway.route;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * DefaultDynamicRouteService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-01-04.
 */
@Component
public class DefaultDynamicRouteService implements ApplicationEventPublisherAware {

  private final RouteDefinitionWriter routeDefinitionWriter;
  private ApplicationEventPublisher publisher;

  @Autowired
  public DefaultDynamicRouteService(RouteDefinitionWriter routeDefinitionWriter) {
    this.routeDefinitionWriter = routeDefinitionWriter;
  }

  public String add(RouteDefinition definition) {
    routeDefinitionWriter.save(Mono.just(definition)).subscribe();
    this.publisher.publishEvent(new RefreshRoutesEvent(this));
    return "success";
  }

  public String update(RouteDefinition definition) {
    try {
      this.routeDefinitionWriter.delete(Mono.just(definition.getId()));
    } catch (Exception e) {
      return "update fail,not find route  routeId: " + definition.getId();
    }
    try {
      routeDefinitionWriter.save(Mono.just(definition)).subscribe();
      this.publisher.publishEvent(new RefreshRoutesEvent(this));
      return "success";
    } catch (Exception e) {
      return "update route  fail";
    }
  }

  public Mono<ResponseEntity<Object>> delete(String id) {
    return this.routeDefinitionWriter
        .delete(Mono.just(id))
        .then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
        .onErrorResume(
            t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }
}
