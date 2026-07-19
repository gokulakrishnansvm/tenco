package com.tenco.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DealerDao {
    @Query("SELECT * FROM dealers ORDER BY name")
    fun observeAll(): Flow<List<DealerEntity>>

    @Query("SELECT * FROM dealers WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<DealerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dealer: DealerEntity)

    @Query("SELECT COUNT(*) FROM dealers")
    suspend fun count(): Int
}

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<PurchaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(purchase: PurchaseEntity)
}

@Dao
interface VendorDao {
    @Query("SELECT * FROM vendors ORDER BY name")
    fun observeAll(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :id")
    fun observeById(id: String): Flow<VendorEntity?>

    @Query("SELECT * FROM vendors WHERE id = :id")
    suspend fun getById(id: String): VendorEntity?

    @Query("SELECT * FROM vendors WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<VendorEntity>

    @Query("SELECT * FROM vendors ORDER BY name LIMIT 1")
    suspend fun firstVendor(): VendorEntity?

    @Query("SELECT * FROM vendors WHERE phone LIKE '%' || :suffix LIMIT 1")
    suspend fun findByPhoneSuffix(suffix: String): VendorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vendor: VendorEntity)
}

@Dao
interface PriceDao {
    @Query("SELECT * FROM prices ORDER BY effectiveFrom DESC")
    fun observeAll(): Flow<List<PriceEntity>>

    @Query("SELECT * FROM prices WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<PriceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(price: PriceEntity)
}

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM deliveries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE vendorId = :vendorId ORDER BY createdAt DESC")
    fun observeForVendor(vendorId: String): Flow<List<DeliveryEntity>>

    @Query("SELECT * FROM deliveries WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<DeliveryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(delivery: DeliveryEntity)

    @Update
    suspend fun update(delivery: DeliveryEntity)

    @Query("SELECT * FROM deliveries WHERE id = :id")
    suspend fun getById(id: String): DeliveryEntity?
}

@Dao
interface ComplaintDao {
    @Query("SELECT * FROM complaints ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE vendorId = :vendorId ORDER BY createdAt DESC")
    fun observeForVendor(vendorId: String): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<ComplaintEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(complaint: ComplaintEntity)

    @Update
    suspend fun update(complaint: ComplaintEntity)

    @Query("SELECT * FROM complaints WHERE id = :id")
    suspend fun getById(id: String): ComplaintEntity?
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE vendorId = :vendorId ORDER BY createdAt DESC")
    fun observeForVendor(vendorId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(payment: PaymentEntity)
}

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox ORDER BY seq")
    suspend fun all(): List<OutboxEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: OutboxEntity)

    @Query("DELETE FROM outbox WHERE seq IN (:seqs)")
    suspend fun deleteBySeqs(seqs: List<Long>)
}
