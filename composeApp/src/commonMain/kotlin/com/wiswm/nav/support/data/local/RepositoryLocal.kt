package com.wiswm.nav.support.data.local

import AppDatabase
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
import com.wiswm.nav.support.util.System
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

class RepositoryLocal(
    private val settingsManager: SettingsManager,
    private val appDatabase: AppDatabase,
    private val system: System
) {
    companion object {
        const val TAG = "RepositoryLocal"
    }

    private var wisUrl: String? = null
    private var wisName: String? = null
    private var sessionKey: String? = null
    private var rosterId: String? = null

    fun setWisUrl(value: String?) {
        wisUrl = value
        settingsManager.wisUrl = value ?: ""
    }

    fun getWisUrl(): String {
        if(wisUrl.isNullOrEmpty()) wisUrl = settingsManager.wisUrl
        return wisUrl!!
    }

    fun setWisName(value: String?) {
        wisName = value
        settingsManager.wisName = value ?: ""
    }

    fun getWisName(): String {
        if (wisName.isNullOrEmpty()) wisName = settingsManager.wisName
        return wisName!!
    }

    fun setSessionKey(value: String?) {
        sessionKey = value
        settingsManager.sessionKey = value ?: ""
    }

    fun getSessionKey(): String {
        if (sessionKey.isNullOrEmpty()) sessionKey = settingsManager.sessionKey
        return sessionKey!!
    }

    fun setRosterId(value: String?){
        rosterId = value
        settingsManager.rosterId = value ?: ""
    }

    fun getRosterId(): String{
        if (rosterId.isNullOrEmpty()) rosterId = settingsManager.rosterId
        return rosterId!!
    }

    fun getLocalGeneratedTransactionId(): Int {
        return settingsManager.transactionId
    }

    suspend fun update(authorizationEntity: AuthorizationEntity) {
        appDatabase.getAuthorizationDao().clear()
        appDatabase.getAuthorizationDao().insert(authorizationEntity)
    }

    suspend fun update(stompEntity: StompEntity) {
        appDatabase.getStompDao().clear()
        appDatabase.getStompDao().insert(stompEntity)
    }

    suspend fun insert(taskEntity: TaskEntity): Long {
        return appDatabase.getTaskDao().insert(taskEntity)
    }

    suspend fun update(taskEntity: TaskEntity) {
        appDatabase.getTaskDao().update(taskEntity)
    }

    suspend fun delete(taskEntity: TaskEntity) {
        appDatabase.getTaskDao().delete(taskEntity)
    }

    fun getAuthorizationEntityAsFlow(): Flow<AuthorizationEntity?> {
        return appDatabase.getAuthorizationDao().getAsFlow()
    }

    suspend fun getAuthorizationEntity(): AuthorizationEntity? {
        return appDatabase.getAuthorizationDao().get()
    }

    fun getDeviceId(): String {
        return system.getDeviceInfo().model
    }

    fun getOsVersion(): String {
        return system.getDeviceInfo().osVersion
    }

    fun getAppVersion(): String {
        return system.getAppVersion()
    }

    fun getCurrentTimeSeconds(): Long {
        return system.getCurrentTimeSeconds()
    }

    @OptIn(ExperimentalTime::class, FormatStringsInDatetimeFormats::class)
    fun getFormattedCurrentTimeSeconds(): String {
        val instant = Instant.fromEpochSeconds(getCurrentTimeSeconds())

        val timezone = TimeZone.currentSystemDefault()
        val dateTime = instant.toLocalDateTime(timezone)

        val formatter = LocalDateTime.Format {
            byUnicodePattern("dd.MM.yyyy HH:mm:ss")
        }

        return dateTime.format(formatter)
    }

    fun getFormattedTimeSeconds(time: Long): String {
        val instant = Instant.fromEpochSeconds(time)

        val timezone = TimeZone.currentSystemDefault()
        val dateTime = instant.toLocalDateTime(timezone)

        val formatter = LocalDateTime.Format {
            byUnicodePattern("dd.MM.yyyy HH:mm:ss")
        }

        return dateTime.format(formatter)
    }

    fun getStompEntityAsFlow(): Flow<StompEntity?> {
        return appDatabase.getStompDao().getAsFlow()
    }

    suspend fun getStompEntity(): StompEntity? {
        return appDatabase.getStompDao().get()
    }

    fun getTaskEntityListAsFlow(): Flow<List<TaskEntity>> {
        return appDatabase.getTaskDao().getAsFlow()
    }

    suspend fun getTaskEntityList(): List<TaskEntity> {
        return appDatabase.getTaskDao().get()
    }

    suspend fun getTaskEntity(taskId: Long): TaskEntity? {
        return appDatabase.getTaskDao().get(taskId)
    }

    suspend fun getTaskEntityByImageFilePath(imageFilePath: String): TaskEntity? {
        return appDatabase.getTaskDao().getByImageFilePath(imageFilePath)
    }

    suspend fun getTaskEntityListByCustomerId(customerId: String): List<TaskEntity>? {
        return appDatabase.getTaskDao().getByCustomerId(customerId)
    }

    suspend fun clearTaskEntity() {
        return appDatabase.getTaskDao().clear()
    }

    suspend fun insert(requestEntity: RequestEntity) {
        appDatabase.getRequestDao().insert(requestEntity)
    }

    suspend fun getRequestEntityList(): List<RequestEntity> {
        return appDatabase.getRequestDao().get()
    }

    suspend fun getLastRequestEntity(): RequestEntity? {
        return appDatabase.getRequestDao().getLast()
    }

    suspend fun delete(requestEntity: RequestEntity) {
        return appDatabase.getRequestDao().delete(requestEntity)
    }

    suspend fun loadBytesFromImagePath(imagePath: String): ByteArray? {
        return system.loadBytesFromPath(imagePath)
    }

    suspend fun insertCustomerEntityList(customerEntityList: List<CustomerEntity>) {
        appDatabase.getCustomerDao().insertAll(customerEntityList)
    }

    suspend fun update(customerEntity: CustomerEntity) {
        appDatabase.getCustomerDao().update(customerEntity)
    }

    suspend fun getCustomerList(): List<CustomerEntity>? {
        return appDatabase.getCustomerDao().getAll()
    }

    fun getCustomerListAsFlow(): Flow<List<CustomerEntity>> {
        return appDatabase.getCustomerDao().getAllAsFlow()
    }

    suspend fun clearCustomerTable() {
        return appDatabase.getCustomerDao().clear()
    }

    suspend fun getByCustomerId(customerId: Int): CustomerEntity? {
        return appDatabase.getCustomerDao().getByCustomerId(customerId)
    }

    suspend fun getSimilarByCustomerId(customerId: Int): List<CustomerEntity>? {
        return appDatabase.getCustomerDao().getSimilarByCustomerId(customerId)
    }

    suspend fun getSimilarByCustomerRefId(customerRefId: Int): List<CustomerEntity>? {
        return appDatabase.getCustomerDao().getSimilarByCustomerRefId(customerRefId)
    }

    suspend fun getSimilarByAddress(address: String): List<CustomerEntity>? {
        return appDatabase.getCustomerDao().getSimilarByAddress(address)
    }

    suspend fun clearNotServicingReasonEntityList() {
        appDatabase.getNotServicingReasonDao().clear()
    }

    suspend fun insertNotServicingReasonEntityList(notServicingReasonEntityList: List<NotServicingReasonEntity>) {
        appDatabase.getNotServicingReasonDao().insert(notServicingReasonEntityList)
    }

    suspend fun getNotServicingReasonEntityList(): List<NotServicingReasonEntity>? {
        return appDatabase.getNotServicingReasonDao().getAll()
    }

    fun getNotServicingReasonEntityListAsFlow(): Flow<List<NotServicingReasonEntity>?> {
        return appDatabase.getNotServicingReasonDao().getAllAsFlow()
    }

    // TruckReport

    suspend fun insertTruckReportEntityList(truckReportEntityList: List<TruckReportEntity>) {
        appDatabase.getTruckReportDao().insert(truckReportEntityList)
    }

    suspend fun updateTruckReportEntity(truckReportEntity: TruckReportEntity) {
        appDatabase.getTruckReportDao().update(truckReportEntity)
    }

    suspend fun getTruckReportEntityList(): List<TruckReportEntity>? {
        return appDatabase.getTruckReportDao().getAll()
    }

    fun getTruckReportEntityListAsFlow(): Flow<List<TruckReportEntity>?> {
        return appDatabase.getTruckReportDao().getAllAsFlow()
    }

    suspend fun clearTruckReportEntityList() {
        appDatabase.getTruckReportDao().clear()
    }

    // CustomerTask

    suspend fun insertCustomerTaskEntityList(customerTaskEntityList: List<CustomerTaskEntity>) {
        appDatabase.getCustomerTaskDao().insertAll(customerTaskEntityList)
    }

    suspend fun updateCustomerTaskEntity(customerTaskEntity: CustomerTaskEntity) {
        appDatabase.getCustomerTaskDao().update(customerTaskEntity)
    }

    suspend fun getCustomerTaskEntityList(): List<CustomerTaskEntity>? {
        return appDatabase.getCustomerTaskDao().getAll()
    }

    fun getCustomerTaskEntityListAsFlow(): Flow<List<CustomerTaskEntity>?> {
        return appDatabase.getCustomerTaskDao().getAllAsFlow()
    }

    suspend fun getCustomerTaskEntityByCustomerIdList(customerId: String): List<CustomerTaskEntity>? {
        return appDatabase.getCustomerTaskDao().getByCustomerId(customerId)
    }

    suspend fun getCustomerTaskEntityByCustomerRefIdList(customerRefId: String): List<CustomerTaskEntity>? {
        return appDatabase.getCustomerTaskDao().getByCustomerRefId(customerRefId)
    }

    fun getCustomerTaskEntityByCustomerIdListAsFlow(customerId: String): Flow<List<CustomerTaskEntity>?> {
        return appDatabase.getCustomerTaskDao().getByCustomerIdAsFlow(customerId)
    }

    fun getCustomerTaskEntityByCustomerRefIdListAsFlow(customerRefId: String): Flow<List<CustomerTaskEntity>?> {
        return appDatabase.getCustomerTaskDao().getByCustomerRefIdAsFlow(customerRefId)
    }

    suspend fun clearCustomerTaskEntityList() {
        appDatabase.getCustomerTaskDao().clear()
    }

    // Waste type

    suspend fun insertWasteTypeEntity(wasteTypeEntity: WasteTypeEntity): Long {
        return appDatabase.getWasteTypeDao().insert(wasteTypeEntity)
    }

    suspend fun insertWasteTypeEntityList(wasteTypeEntityList: List<WasteTypeEntity>){
        appDatabase.getWasteTypeDao().insertAll(wasteTypeEntityList)
    }

    suspend fun updateWasteTypeEntity(wasteTypeEntity: WasteTypeEntity) {
        appDatabase.getWasteTypeDao().update(wasteTypeEntity)
    }

    suspend fun getWasteTypeEntityList(): List<WasteTypeEntity>? {
        return appDatabase.getWasteTypeDao().getAll()
    }

    fun getWasteTypeEntityListAsFlow(): Flow<List<WasteTypeEntity>?> {
        return appDatabase.getWasteTypeDao().getAllAsFlow()
    }

    suspend fun clearWasteTypeEntityList() {
        appDatabase.getWasteTypeDao().clear()
    }

    // Destination

    suspend fun insertDestinationEntity(destinationEntity: DestinationEntity): Long {
        return appDatabase.getDestinationDao().insert(destinationEntity)
    }

    suspend fun insertDestinationEntityList(destinationEntityList: List<DestinationEntity>){
        appDatabase.getDestinationDao().insertAll(destinationEntityList)
    }

    suspend fun updateDestinationEntity(destinationEntity: DestinationEntity) {
        appDatabase.getDestinationDao().update(destinationEntity)
    }

    suspend fun getDestinationEntityList(): List<DestinationEntity>? {
        return appDatabase.getDestinationDao().getAll()
    }

    fun getDestinationEntityListAsFlow(): Flow<List<DestinationEntity>?> {
        return appDatabase.getDestinationDao().getAllAsFlow()
    }

    suspend fun clearDestinationEntityList() {
        appDatabase.getDestinationDao().clear()
    }

    // Job
    suspend fun insertJobEntity(jobEntity: JobEntity): Long {
        return appDatabase.getJobDao().insert(jobEntity)
    }

    suspend fun insertJobEntityList(jobEntityList: List<JobEntity>){
        appDatabase.getJobDao().insertAll(jobEntityList)
    }

    suspend fun updateJobEntity(jobEntity: JobEntity) {
        appDatabase.getJobDao().update(jobEntity)
    }

    suspend fun getJobEntityList(): List<JobEntity>? {
        return appDatabase.getJobDao().getAll()
    }

    fun getJobEntityListAsFlow(): Flow<List<JobEntity>?> {
        return appDatabase.getJobDao().getAllAsFlow()
    }

    suspend fun clearJobEntityList() {
        appDatabase.getJobDao().clear()
    }

    suspend fun clearRepository(){
        setWisUrl(null)
        setWisName(null)
        setSessionKey(null)
        setRosterId(null)
        appDatabase.getAuthorizationDao().clear()
        appDatabase.getStompDao().clear()
        appDatabase.getTaskDao().clear()
        appDatabase.getRequestDao().clear()
        appDatabase.getCustomerDao().clear()
        appDatabase.getNotServicingReasonDao().clear()
        appDatabase.getTruckReportDao().clear()
        appDatabase.getCustomerTaskDao().clear()
        appDatabase.getWasteTypeDao().clear()
        appDatabase.getDestinationDao().clear()
        appDatabase.getJobDao().clear()
    }
}