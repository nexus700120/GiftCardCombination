package ru.smlab.cards

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CardsTest {

    private val useCase = FindBestGiftCardCombinationUseCase()

    @Test
    fun `empty card list`() {
        assertCardsSum(0, useCase(5000, emptyList()))
    }

    @Test
    fun `product price = 5000, cards = 1000, 2000`() =
        assertCardsSum(
            expectedSum = 3000,
            giftCards = useCase(
                productPrice = 5000,
                giftCards = listOf(
                    GiftCard(1000),
                    GiftCard(2000)
                )
            )
        )

    @Test
    fun `product price = 5000, cards = 2000, 3000`() =
        assertCardsSum(
            expectedSum = 5000,
            giftCards = useCase(
                productPrice = 5000,
                giftCards = listOf(
                    GiftCard(2000),
                    GiftCard(3000)
                )
            )
        )

    // AllCardsAreEqual algorithm

    @Test
    fun `product price = 5000, cards = 4000, 4000`() =
        assertCardsSum(
            expectedSum = 4000,
            giftCards = useCase(
                productPrice = 5000,
                giftCards = listOf(
                    GiftCard(4000),
                    GiftCard(4000)
                )
            )
        )

    @Test
    fun `product price = 5000, cards = 3000, 3000`() =
        assertCardsSum(
            expectedSum = 6000,
            giftCards = useCase(
                productPrice = 5000,
                giftCards = listOf(
                    GiftCard(3000),
                    GiftCard(3000)
                )
            )
        )

    @Test
    fun `product price = 3000, cards = 2000, 2000`() =
        assertCardsSum(
            expectedSum = 4000,
            giftCards = useCase(
                productPrice = 3000,
                giftCards = listOf(
                    GiftCard(2000),
                    GiftCard(2000)
                )
            )
        )

    @Test
    fun `product price = 200, cards = 500, 500`() =
        assertCardsSum(
            expectedSum = 0,
            giftCards = useCase(
                productPrice = 200,
                giftCards = listOf(
                    GiftCard(500),
                    GiftCard(500)
                )
            )
        )

    // Dynamic programming

    @Test
    fun `product price = 7, cards = 3, 5`() =
        assertCardsSum(
            expectedSum = 8,
            giftCards = useCase(
                productPrice = 7,
                giftCards = listOf(
                    GiftCard(3),
                    GiftCard(5)
                )
            )
        )

    @Test
    fun `product price = 13000, cards = 5000, 5000, 4000, 4000`() =
        assertCardsSum(
            expectedSum = 1300,
            giftCards = useCase(
                productPrice = 13699,
                giftCards = listOf(
                    GiftCard(5000),
                    GiftCard(4000),
                    GiftCard(5000),
                    GiftCard(4000)
                )
            )
        )

    private fun assertCardsSum(expectedSum: Int, giftCards: List<GiftCard>) =
        assertEquals(expectedSum, giftCards.sumOf { it.price })
}