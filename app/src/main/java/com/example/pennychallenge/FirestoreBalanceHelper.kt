package com.example.pennychallenge

import com.google.firebase.firestore.FirebaseFirestore

private const val PIGGY_BANK_COLLECTION = "piggybank-balance"
private const val USER_FIELD = "user"
private const val BALANCE_FIELD = "balance"

object FirestoreBalanceHelper {

	fun fetchBalanceForUser(
		firestore: FirebaseFirestore,
		user: String,
		onSuccess: (Long?) -> Unit,
		onFailure: (Exception) -> Unit,
	) {
		firestore.collection(PIGGY_BANK_COLLECTION)
			.whereEqualTo(USER_FIELD, user)
			.limit(1)
			.get()
			.addOnSuccessListener { snapshot ->
				val balanceValue = snapshot.documents
					.firstOrNull()
					?.get(BALANCE_FIELD)

				val balance = when (balanceValue) {
					is Number -> balanceValue.toLong()
					is String -> balanceValue.toLongOrNull()
					else -> null
				}

				onSuccess(balance)
			}
			.addOnFailureListener(onFailure)
	}

	fun upsertBalanceForUser(
		firestore: FirebaseFirestore,
		user: String,
		balance: Long,
		onSuccess: () -> Unit,
		onFailure: (Exception) -> Unit,
	) {
		val payload = mapOf(
			USER_FIELD to user,
			BALANCE_FIELD to balance,
		)

		firestore.collection(PIGGY_BANK_COLLECTION)
			.whereEqualTo(USER_FIELD, user)
			.limit(1)
			.get()
			.addOnSuccessListener { snapshot ->
				val document = snapshot.documents.firstOrNull()
				if (document != null) {
					document.reference.set(payload)
						.addOnSuccessListener { onSuccess() }
						.addOnFailureListener(onFailure)
				} else {
					firestore.collection(PIGGY_BANK_COLLECTION)
						.add(payload)
						.addOnSuccessListener { onSuccess() }
						.addOnFailureListener(onFailure)
				}
			}
			.addOnFailureListener(onFailure)
	}
}

