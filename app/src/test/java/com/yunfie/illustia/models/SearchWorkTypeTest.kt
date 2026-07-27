package com.yunfie.illustia.models

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

class SearchWorkTypeTest {

    @Test
    fun `all artwork option accepts illustrations manga and ugoira`() {
        SearchWorkType.Artworks.acceptsIllustType("illust").shouldBeTrue()
        SearchWorkType.Artworks.acceptsIllustType("manga").shouldBeTrue()
        SearchWorkType.Artworks.acceptsIllustType("ugoira").shouldBeTrue()
    }

    @Test
    fun `illustrations and ugoira option excludes manga`() {
        SearchWorkType.IllustrationsAndUgoira.acceptsIllustType("illust").shouldBeTrue()
        SearchWorkType.IllustrationsAndUgoira.acceptsIllustType("ugoira").shouldBeTrue()
        SearchWorkType.IllustrationsAndUgoira.acceptsIllustType("manga").shouldBeFalse()
    }

    @Test
    fun `single artwork options only accept their matching API type`() {
        SearchWorkType.Illustrations.acceptsIllustType("illust").shouldBeTrue()
        SearchWorkType.Illustrations.acceptsIllustType("ugoira").shouldBeFalse()
        SearchWorkType.Ugoira.acceptsIllustType("ugoira").shouldBeTrue()
        SearchWorkType.Ugoira.acceptsIllustType("illust").shouldBeFalse()
        SearchWorkType.Manga.acceptsIllustType("manga").shouldBeTrue()
        SearchWorkType.Manga.acceptsIllustType("illust").shouldBeFalse()
    }

    @Test
    fun `novel option is routed away from illustration results`() {
        SearchWorkType.Novels.isNovel.shouldBeTrue()
        SearchWorkType.Novels.acceptsIllustType("illust").shouldBeFalse()
        SearchWorkType.Artworks.isNovel.shouldBeFalse()
    }
}
