package com.humans.aura.core.presentation.components

import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownMessageTextTest {

    @Test
    fun markdown_to_annotated_string_preserves_plain_text_output() {
        val rendered = markdownToAnnotatedString("Hello **world**\n\n- one\n- two")

        assertTrue(rendered.text.contains("Hello"))
        assertTrue(rendered.text.contains("world"))
        assertTrue(rendered.text.contains("one"))
        assertTrue(rendered.text.contains("two"))
    }

    @Test
    fun markdown_to_annotated_string_preserves_link_labels() {
        val rendered = markdownToAnnotatedString("Read [the summary](https://example.com) next")

        assertTrue(rendered.text.contains("Read"))
        assertTrue(rendered.text.contains("the summary"))
        assertTrue(rendered.text.contains("next"))
    }
}
