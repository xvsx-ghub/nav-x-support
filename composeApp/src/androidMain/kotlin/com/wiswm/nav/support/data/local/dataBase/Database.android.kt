import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

lateinit var roomContext: Context

actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val context: Context = roomContext
    val dbFile = context.applicationContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, dbFile.absolutePath)
        .fallbackToDestructiveMigration(dropAllTables = true)
}