package com.abinbev.b2b.price.api.config.properties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
class ToggleConfigurationPropertiesTest {

    public static final String FIRST_TOGGLE = "firstFakeToggleEnabled";
    public static final String SECOND_TOGGLE = "secondFakeToggleFalse";
    public static final String OTHER_TOGGLE = "otherFakeToggleFalse";

    @InjectMocks
    private ToggleConfigurationProperties codeToggleConfig;

    @Test
    void shouldReturnTrueWhenCodeToggleIsPresentAndIsTrue() {

        final Map<String, Boolean> fakeToggles = new HashMap<>();
        fakeToggles.put(FIRST_TOGGLE, true);
        fakeToggles.put(SECOND_TOGGLE, false);

        codeToggleConfig.setCode(fakeToggles);

        assertThat(codeToggleConfig.isEnabledCodeToggle(FIRST_TOGGLE), is(equalTo(true)));
    }

    @Test
    void shouldReturnFalseWhenCodeToggleIsPresentAndIsFalse() {

        final Map<String, Boolean> fakeToggles = new HashMap<>();
        fakeToggles.put(FIRST_TOGGLE, true);
        fakeToggles.put(SECOND_TOGGLE, false);

        codeToggleConfig.setCode(fakeToggles);

        assertThat(codeToggleConfig.isEnabledCodeToggle(SECOND_TOGGLE), is(equalTo(false)));
    }

    @Test
    void shouldReturnFalseWhenCodeToggleIsNotPresent() {

        final Map<String, Boolean> fakeToggles = new HashMap<>();
        fakeToggles.put(FIRST_TOGGLE, true);
        fakeToggles.put(SECOND_TOGGLE, false);

        codeToggleConfig.setCode(fakeToggles);

        assertThat(codeToggleConfig.isEnabledCodeToggle(OTHER_TOGGLE), is(equalTo(false)));
    }

}