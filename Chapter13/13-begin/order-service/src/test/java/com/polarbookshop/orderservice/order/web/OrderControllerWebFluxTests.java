package com.polarbookshop.orderservice.order.web;

import com.polarbookshop.orderservice.config.SecurityConfig;
import com.polarbookshop.orderservice.order.domain.Order;
import com.polarbookshop.orderservice.order.domain.OrderService;
import com.polarbookshop.orderservice.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@WebFluxTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerWebFluxTests {

	@Autowired
	private WebTestClient webClient;

	@MockBean
	OrderService orderService;

	@MockBean
	ReactiveJwtDecoder reactiveJwtDecoder;

	@Test
	void whenBookNotAvailableThenRejectOrder() {
		OrderRequest orderRequest = new OrderRequest("1234567890", 3);
		Order expectedOrder = OrderService.buildRejectedOrder(orderRequest.isbn(), orderRequest.quantity());
		given(orderService.submitOrder(orderRequest.isbn(), orderRequest.quantity()))
				.willReturn(Mono.just(expectedOrder));

		Order createdOrder = webClient.mutateWith(mockJwt()).mutateWith(csrf())
				.post().uri("/orders/")
				.bodyValue(orderRequest)
				.exchange()
				.expectStatus().is2xxSuccessful()
				.expectBody(Order.class).returnResult().getResponseBody();

		assertThat(createdOrder).isNotNull();
		assertThat(createdOrder.status()).isEqualTo(OrderStatus.REJECTED);
	}

}
