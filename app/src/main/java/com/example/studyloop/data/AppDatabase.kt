package com.example.studyloop.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Defines every table stored in StudyLoop's offline Room database.
@Database(
    entities = [
        SubjectEntity::class,
        TopicEntity::class,
        FlashcardEntity::class,
        RevisionSessionEntity::class,
        QuizResultEntity::class,
        QuizEntity::class,
        QuizQuestionEntity::class,
        NoteEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Provides access to each database table.
    abstract fun subjectDao(): SubjectDao
    abstract fun topicDao(): TopicDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun revisionDao(): RevisionDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun quizDao(): QuizDao
    abstract fun noteDao(): NoteDao

    companion object {

        // Prevents multiple database instances from being created.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Creates the topic table for older installations.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `topics` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subjectId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_topics_subjectId`
                    ON `topics` (`subjectId`)
                    """.trimIndent()
                )
            }
        }

        // Creates the original quiz-result table.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quiz_results` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `score` INTEGER NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `completedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // Adds user-created quizzes, questions and improved results.
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                    ALTER TABLE `quiz_results`
                    ADD COLUMN `quizId` INTEGER
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    ALTER TABLE `quiz_results`
                    ADD COLUMN `quizTitle`
                    TEXT NOT NULL DEFAULT 'Practice Quiz'
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quizzes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subjectId` INTEGER,
                        `createdAt` INTEGER NOT NULL,
                        FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`)
                            ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_quizzes_subjectId`
                    ON `quizzes` (`subjectId`)
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `quiz_questions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `quizId` INTEGER NOT NULL,
                        `question` TEXT NOT NULL,
                        `optionA` TEXT NOT NULL,
                        `optionB` TEXT NOT NULL,
                        `optionC` TEXT NOT NULL,
                        `optionD` TEXT NOT NULL,
                        `correctAnswerIndex` INTEGER NOT NULL,
                        `explanation` TEXT NOT NULL,
                        FOREIGN KEY(`quizId`) REFERENCES `quizzes`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_quiz_questions_quizId`
                    ON `quiz_questions` (`quizId`)
                    """.trimIndent()
                )
            }
        }

        // Adds offline notes without deleting existing application data.
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `notes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `topicId` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`topicId`) REFERENCES `topics`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )

                db.execSQL(
                    """
                    CREATE INDEX IF NOT EXISTS
                    `index_notes_topicId`
                    ON `notes` (`topicId`)
                    """.trimIndent()
                )
            }
        }

        // Returns the same database instance throughout the application.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studyloop_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also {
                        INSTANCE = it
                    }
            }
        }
    }
}