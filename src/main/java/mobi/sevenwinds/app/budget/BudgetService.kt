package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val author = body.authorId?.let { AuthorEntity.findById(it) }
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = author
            }
            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = if (param.authorName.isNullOrEmpty()) {
                BudgetTable
                    .select {
                        BudgetTable.year eq param.year
                    }
            } else {
                BudgetTable
                    .leftJoin(AuthorTable, { authorId }, { AuthorTable.id })
                    .select {
                        BudgetTable.year eq param.year and
                                (AuthorTable.fullName.lowerCase() like "%${param.authorName.toLowerCase()}%")
                    }
            }
                .limit(param.limit, param.offset)
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)


            val total = BudgetTable
                .select { BudgetTable.year eq param.year }
                .count()


            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            val sumByType = BudgetTable
                .slice(BudgetTable.type, BudgetTable.amount.sum())
                .select { BudgetTable.year eq param.year }
                .groupBy(BudgetTable.type)
                .associate { it[BudgetTable.type].name to it[BudgetTable.amount.sum()]!! }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}