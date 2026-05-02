package com.shipmonk.testingday.modules.rates.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.shipmonk.testingday.modules.rates.converter.RatesMapConverter;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatesMapConverterTest {

    private RatesMapConverter converter;

    @BeforeEach
    void setup() {
        converter = new RatesMapConverter();
    }

    @Test
    void convertToDatabaseColumn_serializesMapToJsonString() throws Exception {
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        rates.put("USD", BigDecimal.ONE);
        rates.put("GBP", new BigDecimal("0.809524"));

        String json = converter.convertToDatabaseColumn(rates);

        JSONAssert.assertEquals(
                "{\"USD\":1,\"GBP\":0.809524}",
                json,
                JSONCompareMode.LENIENT);
    }

    @Test
    void convertToEntityAttribute_deserializesJsonStringToMap() {
        String json = "{\"USD\":1,\"GBP\":0.809524,\"JPY\":133.333333}";

        Map<String, BigDecimal> rates = converter.convertToEntityAttribute(json);

        assertThat(rates).hasSize(3);
        assertThat(rates.get("USD")).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(rates.get("GBP")).isEqualByComparingTo(new BigDecimal("0.809524"));
        assertThat(rates.get("JPY")).isEqualByComparingTo(new BigDecimal("133.333333"));
    }

    @Test
    void roundtrip_preservesBigDecimalPrecision() {
        Map<String, BigDecimal> original = new LinkedHashMap<>();
        original.put("USD", BigDecimal.ONE);
        original.put("GBP", new BigDecimal("0.809524"));
        original.put("JPY", new BigDecimal("133.333333"));

        Map<String, BigDecimal> roundtripped = converter.convertToEntityAttribute(
                converter.convertToDatabaseColumn(original));

        assertThat(roundtripped).hasSize(original.size());
        original.forEach((currency, value) -> assertThat(roundtripped.get(currency)).isEqualByComparingTo(value));
    }

    @Test
    void convertToDatabaseColumn_emptyMap_returnsEmptyJsonObject() {
        String json = converter.convertToDatabaseColumn(Map.of());

        assertThat(json).isEqualTo("{}");
    }

    @Test
    void convertToEntityAttribute_emptyJsonObject_returnsEmptyMap() {
        Map<String, BigDecimal> rates = converter.convertToEntityAttribute("{}");

        assertThat(rates).isNotNull().isEmpty();
    }

    @Test
    void convertToEntityAttribute_malformedJson_throwsIllegalStateException() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("not valid json"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to deserialize rates map");
    }
}
