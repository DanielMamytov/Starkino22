package co.nisari.katisnar.presentation.data.di

import co.nisari.katisnar.presentation.data.local.ArticleDao
import co.nisari.katisnar.presentation.data.local.ChecklistDao
import co.nisari.katisnar.presentation.data.local.NoteDao
import co.nisari.katisnar.presentation.data.local.StarDatabase
import co.nisari.katisnar.presentation.data.repository.ArticleRepository
import co.nisari.katisnar.presentation.data.repository.ChecklistRepository
import co.nisari.katisnar.presentation.data.repository.NoteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object NotesModule {

    @Provides fun provideNoteDao(db: StarDatabase) = db.noteDao()
    @Provides fun provideChecklistDao(db: StarDatabase) = db.checklistDao()
    @Provides fun provideArticleDao(db: StarDatabase) = db.articleDao()

    @Provides fun provideNoteRepository(dao: NoteDao) = NoteRepository(dao)
    @Provides fun provideChecklistRepository(dao: ChecklistDao) = ChecklistRepository(dao)
    @Provides fun provideArticleRepository(dao: ArticleDao) = ArticleRepository(dao)
}
