package ru.smlab.cards

import kotlin.math.max

class FindBestGiftCardCombinationUseCase {

    operator fun invoke(
        productPrice: Int,
        giftCards: List<GiftCard>
    ): List<GiftCard> {
        var areAllCardEqual = true
        val firstCardPrice = giftCards.firstOrNull()?.price ?: 0
        var cardsSum = firstCardPrice
        for (index in 1 until giftCards.size) {
            val card = giftCards[index]
            cardsSum += card.price
            if (areAllCardEqual) {
                areAllCardEqual = card.price == firstCardPrice
            }
        }
        val algorithm = when {
            cardsSum == 0 -> ZeroCardsSum()
            cardsSum <= productPrice -> CardsSumAreLessOrEqualProductPrice()
            areAllCardEqual -> AllCardsAreEqual()
            else -> DynamicProgrammingAlgorithm()
        }
        return algorithm.find(productPrice, giftCards)
    }

    private interface FindAlgorithm {
        fun find(productPrice: Int, giftCards: List<GiftCard>): List<GiftCard>
    }

    private class ZeroCardsSum : FindAlgorithm {
        override fun find(productPrice: Int, giftCards: List<GiftCard>): List<GiftCard> = emptyList()
    }

    private class CardsSumAreLessOrEqualProductPrice : FindAlgorithm {
        override fun find(productPrice: Int, giftCards: List<GiftCard>): List<GiftCard> = giftCards
    }

    private class AllCardsAreEqual : FindAlgorithm {

        // Sum of cards is always greater than product price
        override fun find(productPrice: Int, giftCards: List<GiftCard>): List<GiftCard> {
            val cardDenomination = giftCards.first().price
            var numberOfCards = productPrice / cardDenomination
            val needToPay = productPrice - numberOfCards * cardDenomination
            val burntPrice = cardDenomination - needToPay
            if (burntPrice <= needToPay) {
                numberOfCards++
            }
            return giftCards.take(numberOfCards)
        }
    }

    // https://en.wikipedia.org/wiki/Knapsack_problem
    private class DynamicProgrammingAlgorithm : FindAlgorithm {

        // Sum of cards is always greater than product price
        override fun find(productPrice: Int, giftCards: List<GiftCard>): List<GiftCard> {
            val builder = Table.Builder()
            for (index in 0..productPrice) {
                builder.addColumn(index)
            }
            for (card in giftCards) {
                builder.addRow(card.price)
            }
            val table = builder.build()
            fillTable(table, 0, 0)

            val needToPayCell = table.getCell(table.rowsSize - 1, table.columnsSize - 1)
            val needToPayCombination = needToPayCell?.giftCards ?: emptyList()
            val needToPaySum = productPrice - (needToPayCell?.sum ?: 0)
            println(
                "needToPaySum = $needToPaySum, " +
                        "needToPayCombination = " +
                        needToPayCombination.joinToString(prefix = "[", postfix = "]") { it.price.toString() }
            )
            if (productPrice - needToPaySum > 0) {
                // Calculate burnt price


            }
            return table.getCell(table.rowsSize - 1, table.columnsSize - 1)?.giftCards ?: emptyList()
        }

        private fun fillTable(table: Table, startRowIndex: Int, startColumnIndex: Int) {
            println("fillTable")
            for (rowIndex in startRowIndex until table.rowsSize) {
                for (columnIndex in startColumnIndex until table.columnsSize) {
                    val columnProductPrice = table.productPrice(columnIndex)
                    val rowCardPrice = table.cardPrice(rowIndex)

                    val cellAbove = table.getCell(rowIndex - 1, columnIndex)
                    val cellAboveSum = cellAbove?.sum ?: 0

                    var currentSum = if (columnProductPrice >= rowCardPrice) rowCardPrice else 0
                    var freeSpaceCell: Table.Cell? = null
                    if (currentSum > 0 && columnProductPrice - currentSum > 0) {
                        // There is an empty space in the knapsack
                        freeSpaceCell = table.getCell(rowIndex - 1, columnIndex - rowCardPrice)
                        if (freeSpaceCell != null) {
                            currentSum += freeSpaceCell.sum
                        }
                    }

                    if (cellAboveSum > 0 || currentSum > 0) {
                        if (cellAboveSum > currentSum) {
                            table.setCell(rowIndex, columnIndex, cellAbove!!)
                        } else {
                            val cell = Table.Cell(
                                currentSum,
                                buildList {
                                    add(GiftCard(rowCardPrice))
                                    if (freeSpaceCell != null) {
                                        addAll(freeSpaceCell.giftCards)
                                    }
                                }
                            )
                            table.setCell(rowIndex, columnIndex, cell)
                        }
                    }
                }
            }
            println(table)
        }

        private class Table private constructor(
            private val columns: MutableList<Int>,
            private val rows: MutableList<Int>,
            private val table: MutableList<MutableList<Cell>>
        ) {
            val columnsSize: Int = columns.size
            val rowsSize: Int = rows.size

            fun productPrice(columnIndex: Int): Int = columns[columnIndex]

            fun cardPrice(rowIndex: Int): Int = rows[rowIndex]

            fun getCell(rowIndex: Int, columnIndex: Int): Cell? =
                table.getOrNull(rowIndex)?.getOrNull(columnIndex)

            fun setCell(rowIndex: Int, columnIndex: Int, cell: Cell) {
                table.getOrNull(rowIndex)?.set(columnIndex, cell)
            }

            fun builder(): Builder =
                Builder(columns, rows, table)

            // For debug only
            override fun toString(): String {
                var maxCellWidth = max(
                    columns.max().toString().length,
                    rows.max().toString().length
                )
                for (rowIndex in rows.indices) {
                    val row = table[rowIndex]
                    maxCellWidth = max(
                        maxCellWidth,
                        row.maxOf { it.sum }.toString().length
                    )
                }
                return buildString {
                    for (columnIndex in 0..columns.size) {
                        append("|")
                        if (columnIndex == 0) {
                            append("".padEnd(maxCellWidth))
                        } else {
                            append(columns[columnIndex - 1].toString().padEnd(maxCellWidth))
                        }
                    }
                    append("|\n")
                    for (rowIndex in 0 until rows.size) {
                        val row = rows[rowIndex]
                        append("|")
                        append(row.toString().padEnd(maxCellWidth))
                        val cells = table[rowIndex]
                        for (cell in cells) {
                            append("|")
                            append(cell.sum.toString().padEnd(maxCellWidth))
                        }
                        append("|\n")
                    }
                }
            }

            class Cell(
                val sum: Int,
                val giftCards: List<GiftCard>
            )

            class Builder {

                private val columns: MutableList<Int>
                private val rows: MutableList<Int>
                private val table: MutableList<MutableList<Cell>>?

                constructor(
                    initialColumns: MutableList<Int>,
                    initialRows: MutableList<Int>,
                    initialTable: MutableList<MutableList<Cell>>?
                ) {
                    columns = initialColumns
                    rows = initialRows
                    table = initialTable
                }

                constructor() : this(mutableListOf(), mutableListOf(), null)

                fun addColumn(productPriceStep: Int) {
                    columns.add(productPriceStep)
                }

                fun addRow(cardPrice: Int) {
                    rows.add(cardPrice)
                }

                fun build(): Table {
                    if (table == null) {
                        // There is no previous table
                        return Table(
                            columns = columns,
                            rows = rows,
                            table = MutableList(rows.size) {
                                MutableList(columns.size) {
                                    Cell(0, emptyList())
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}