package com.nobg.chesstracker2.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import org.junit.jupiter.api.Test;

class TrainingDayFormTest {

    @Test
    void entriesAreTypedForSpringFormBinding() throws Exception {
        Type fieldType = TrainingDayForm.class.getDeclaredField("entries").getGenericType();
        Type getterType = TrainingDayForm.class.getDeclaredMethod("getEntries").getGenericReturnType();
        Type setterType = TrainingDayForm.class.getDeclaredMethod("setEntries", List.class).getGenericParameterTypes()[0];

        assertEntryListType(fieldType);
        assertEntryListType(getterType);
        assertEntryListType(setterType);
    }

    private void assertEntryListType(Type type) {
        assertThat(type).isInstanceOf(ParameterizedType.class);
        ParameterizedType parameterizedType = (ParameterizedType) type;
        assertThat(parameterizedType.getRawType()).isEqualTo(List.class);
        assertThat(parameterizedType.getActualTypeArguments()).containsExactly(TrainingEntryForm.class);
    }
}
