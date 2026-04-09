import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wiswm.nav.support.data.local.dataBase.Converters
import com.wiswm.nav.support.data.local.dataBase.dao.AuthorizationDao
import com.wiswm.nav.support.data.local.dataBase.dao.CustomerDao
import com.wiswm.nav.support.data.local.dataBase.dao.CustomerTaskDao
import com.wiswm.nav.support.data.local.dataBase.dao.DestinationDao
import com.wiswm.nav.support.data.local.dataBase.dao.JobDao
import com.wiswm.nav.support.data.local.dataBase.dao.NotServicingReasonDao
import com.wiswm.nav.support.data.local.dataBase.dao.RequestDao
import com.wiswm.nav.support.data.local.dataBase.dao.StompDao
import com.wiswm.nav.support.data.local.dataBase.dao.TaskDao
import com.wiswm.nav.support.data.local.dataBase.dao.TruckReportDao
import com.wiswm.nav.support.data.local.dataBase.dao.WasteTypeDao
import com.wiswm.nav.support.data.local.dataBase.entity.AuthorizationEntity
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerEntity
import com.wiswm.nav.support.data.local.dataBase.entity.CustomerTaskEntity
import com.wiswm.nav.support.data.local.dataBase.entity.DestinationEntity
import com.wiswm.nav.support.data.local.dataBase.entity.JobEntity
import com.wiswm.nav.support.data.local.dataBase.entity.NotServicingReasonEntity
import com.wiswm.nav.support.data.local.dataBase.entity.RequestEntity
import com.wiswm.nav.support.data.local.dataBase.entity.StompEntity
import com.wiswm.nav.support.data.local.dataBase.entity.TaskEntity
import com.wiswm.nav.support.data.local.dataBase.entity.TruckReportEntity
import com.wiswm.nav.support.data.local.dataBase.entity.WasteTypeEntity

@Database(
    entities = [
        AuthorizationEntity::class,
        StompEntity::class,
        TaskEntity::class,
        RequestEntity::class,
        CustomerEntity::class,
        NotServicingReasonEntity::class,
        CustomerTaskEntity::class,
        TruckReportEntity::class,
        WasteTypeEntity::class,
        DestinationEntity::class,
        JobEntity::class
    ],
    version = 35
)

@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getAuthorizationDao(): AuthorizationDao
    abstract fun getStompDao(): StompDao
    abstract fun getTaskDao(): TaskDao
    abstract fun getRequestDao(): RequestDao
    abstract fun getCustomerDao(): CustomerDao
    abstract fun getNotServicingReasonDao(): NotServicingReasonDao
    abstract fun getCustomerTaskDao(): CustomerTaskDao
    abstract fun getTruckReportDao(): TruckReportDao
    abstract fun getWasteTypeDao(): WasteTypeDao
    abstract fun getDestinationDao(): DestinationDao
    abstract fun getJobDao(): JobDao
}