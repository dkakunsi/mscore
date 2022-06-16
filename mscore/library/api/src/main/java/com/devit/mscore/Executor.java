package com.devit.mscore;

import com.devit.mscore.exception.ApplicationRuntimeException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * <p>
 * Interface to mediate the interaction with object.
 * </p>
 * 
 * @author dkakunsi
 */
public interface Executor<T> {

    /**
     * Add object {@code t} to execution list.
     * 
     * @param object to add.
     */
    void add(T object);

    /**
     * Execute objects on {@code json} data.
     * <p>
     * The objects will be executed on demand
     * </p>
     * 
     * @param context application context.
     * @param data    to execute.
     * @throws ApplicationRuntimeException error in execution.
     */
    void execute(ApplicationContext context, JSONObject data) throws ApplicationRuntimeException;

    /**
     * Execute objects on {@code jsons} data.
     * 
     * @param context application context.
     * @param dataArray   to execute.
     * @throws ApplicationRuntimeException error in execution.
     */
    default void execute(ApplicationContext context, JSONArray dataArray) throws ApplicationRuntimeException {
        dataArray.forEach(data -> execute(context, (JSONObject) data));
    }
}
