package com.salahabusaif.financemanager.core.data.profile

import android.content.Context
import com.salahabusaif.financemanager.core.database.FinanceDatabase
import com.salahabusaif.financemanager.core.database.entity.OwnerProfileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Properties
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class OwnerProfile(
    val fullName: String = "",
    val phoneNumber: String = "",
    val bankOfPalestineReference: String = "",
)

interface OwnerProfileRepository {
    val profile: Flow<OwnerProfile>

    suspend fun bootstrapIfAbsent()

    suspend fun update(profile: OwnerProfile)
}

@Singleton
class RoomOwnerProfileRepository @Inject constructor(
    private val database: FinanceDatabase,
    @ApplicationContext private val context: Context,
) : OwnerProfileRepository {
    override val profile: Flow<OwnerProfile> =
        database.ledgerDao().observeOwnerProfile(OWNER_PROFILE_ID).map { it?.toDomain() ?: OwnerProfile() }

    override suspend fun bootstrapIfAbsent() {
        val existing = database.ledgerDao().ownerProfile(OWNER_PROFILE_ID)
        if (existing != null && !existing.fullName.contains("Ø")) return
        val properties = Properties()
        runCatching {
            context.assets.open(PRIVATE_PROFILE_ASSET).bufferedReader(StandardCharsets.UTF_8).use(properties::load)
        }
        val profile = OwnerProfile(
            fullName = properties.getProperty("owner.fullName").orEmpty(),
            phoneNumber = properties.getProperty("owner.phoneNumber").orEmpty(),
            bankOfPalestineReference = properties.getProperty("owner.bankOfPalestineReference").orEmpty(),
        )
        if (profile.fullName.isNotBlank() || profile.phoneNumber.isNotBlank() || profile.bankOfPalestineReference.isNotBlank()) {
            update(profile)
        }
    }

    override suspend fun update(profile: OwnerProfile) {
        database.ledgerDao().upsertOwnerProfile(
            OwnerProfileEntity(
                id = OWNER_PROFILE_ID,
                fullName = profile.fullName,
                phoneNumber = profile.phoneNumber,
                bankOfPalestineReference = profile.bankOfPalestineReference,
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }
}

private fun OwnerProfileEntity.toDomain() = OwnerProfile(fullName, phoneNumber, bankOfPalestineReference)

const val OWNER_PROFILE_ID = "local-owner"
private const val PRIVATE_PROFILE_ASSET = "owner-profile.properties"
