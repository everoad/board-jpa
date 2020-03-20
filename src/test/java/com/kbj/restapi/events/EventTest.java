package com.kbj.restapi.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

public class EventTest {

  @Test
  public void builder() {
    Event event = Event.builder()
                    .name("Hello World!!")
                    .description("REST API!!")     
                    .build();
    assertThat(event).isNotNull();
  }


  @Test
  public void javaBean() {
    // Given
    String name = "event";
    String description = "Spring";
    
    // When
    Event event = new Event();
    event.setName(name);
    event.setDescription(description);

    // Then
    assertThat(event.getName()).isEqualTo(name);
    assertThat(event.getDescription()).isEqualTo(description);
  }
  

  @ParameterizedTest
  @MethodSource("paramsForTestFree")
  public void testFree(int basePrice, int maxPrice, boolean isFree) {
    // Given
    Event event = Event.builder()
      .basePrice(basePrice)
      .maxPrice(maxPrice)
      .build();

    // When
    event.update();

    // Then
    assertThat(event.isFree()).isEqualTo(isFree);
  }


  private static Stream<Arguments> paramsForTestFree() {
    return Stream.of(
      Arguments.of(0, 0, true),
      Arguments.of(100, 0, false),
      Arguments.of(0, 100, false)
    );
  }


  @ParameterizedTest
  @MethodSource("paramsForTestOffline")
  public void testOffline(String location, boolean isOffline) {
    // Given
    Event event = Event.builder()
      .location(location)
      .build();

    // When
    event.update();

    // Then
    assertThat(event.isOffline()).isEqualTo(isOffline);
  }


  private static Stream<Arguments> paramsForTestOffline() {
    return Stream.of(
      Arguments.of("Hello Location", true),
      Arguments.of(null, false)
    );
  }

}