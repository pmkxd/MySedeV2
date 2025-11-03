package com.test.mysede.DAO;

/**
 * Callback genérico para conocer el resultado de una operación en Firestore.
 */
public interface FirestoreOperationCallback {

    /**
     * Se invoca cuando la operación finaliza de forma exitosa.
     */
    void onSuccess();

    /**
     * Se invoca cuando ocurre un error durante la operación.
     *
     * @param exception error reportado por Firebase.
     */
    void onFailure(Exception exception);
}