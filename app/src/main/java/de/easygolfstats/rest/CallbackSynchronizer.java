package de.easygolfstats.rest;

import androidx.annotation.WorkerThread;

import java.util.HashMap;

import de.easygolfstats.types.CallbackResult;

@WorkerThread
public class CallbackSynchronizer {
    private static HashMap<Integer, Semaphore> map = new HashMap<>();
    private static CallbackSynchronizer synchronizer = new CallbackSynchronizer();


    /**
     * This method should be called by the callback methods
     * to 'release' the waiting methods to the callback.
     *
     * @param requestId    Callback methods deliver an ID as a result which can be used later to
     *                     identify the relation between the initial call and the later incoming callback.
     * @param callBackName Name of the callback (used for information only - e.g. logging)
     */
    public static void callBackCalled(final int requestId, final CallbackResult callbackResult, final String callBackName) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread.currentThread().setName("SemaphoreCreateWaiter");

                Semaphore sem = null;
                int i = 0;
                while (sem == null) {
                    sem = synchronizer.retrieveSemaphore(requestId);
                    try {
                        Thread.sleep(200);
                        if (i++ > 2) {
                            break;
                        }
                    } catch (InterruptedException ie) {
                    }
                }
                if (null != sem) {
                    sem.update(requestId, callbackResult);
                    sem.deactivate();
                }
                ;
            }
        });
        thread.start();
    }

    /**
     * Returns an ApiError after waiting for a callback
     *
     * @param requestId    The ID to identify the relation between the MTI call and the callback of MTI.
     *                      This ID is used to wait for the callback.
     * @param semaphoreInfo String which can be used to identify the semaphore.
     * @param timeOut       Max waiting time in millis. If null there is no timeout.
     * @return If successfull ApiError.OK otherwise another ApiError
     */
    public static CallbackResult wait(int requestId, String semaphoreInfo, Long timeOut) {
        return getSemaphoreForCallBack(requestId, semaphoreInfo).waitForCallback(timeOut);
    }

    /*
     * Returns a semaphore which can be used for waiting to callbacks
     * @param callBackId The ID to identify the relation between the MTI call and the callback of MTI.
     *                   This ID is used to wait for the callback.
     * @param semaphoreInfo String which can be used to identify the semaphore.
     * @return A semaphore which can be used to wait until a callback comes
     */
    private static Semaphore getSemaphoreForCallBack(int callBackId, String semaphoreInfo) {
        Semaphore sem = retrieveSemaphore(callBackId);
        if (null != sem && sem.isWaiting()) {
            // if still there was a semaphore waiting to the same callBackId
            // in this version this means that the last call was not successfull and we return a MESSAGE NOT SEND
            Semaphore dummySem = addSemaphore(-1, semaphoreInfo);
            dummySem.callbackResult = CallbackResult.MESSAGE_NOT_SEND;
            dummySem.interruptByError();
            return dummySem;
        }
        if (null != sem) {
            removeSemaphore(callBackId);
        }

        return addSemaphore(callBackId, semaphoreInfo);
    }

    private static void setUserInterrupt(int callBack) {
        synchronizer.interruptByUser(callBack);
    }

    private static Semaphore addSemaphore(int requestId, String info) {
        return synchronizer.createSemaphore(requestId, info);
    }

    private static void removeSemaphore(int requestId) {
        synchronizer.dropSemaphore(requestId);
    }

    public static void setInterruptedByUser(int requestId) {
        synchronizer.setUserInterrupt(requestId);
    }

    private static void setInterruptedByError(int requestId) {
        synchronizer.setErrorInterrupt(requestId);
    }

    private Semaphore createSemaphore(int requestId, String info) {
        Semaphore sem = new Semaphore(info);
        map.put(requestId, sem);
        return sem;
    }

    private static Semaphore retrieveSemaphore(int callBack) {
        if (map.containsKey(callBack)) {
            return map.get(callBack);
        }
        return null;
    }

    private void dropSemaphore(int callBack) {
        if (map.containsKey(callBack)) {
            map.remove(callBack);
        }
    }

    private void interruptByUser(int callBack) {
        Semaphore sem = retrieveSemaphore(callBack);
        if (null != sem) {
            sem.interrruptByUser();
        }
    }

    private void setErrorInterrupt(int callBack) {
        Semaphore sem = retrieveSemaphore(callBack);
        if (null != sem) {
            sem.interruptByError();
        }
    }

    // ============================================================
    //      The class Semaphore is used for waiting to callbacks
    // ============================================================
    private class Semaphore {
        private int requestId;
        private String semaphoreInfo;
        private CallbackResult callbackResult = CallbackResult.OK;
        private boolean interruptedByUser = false;
        private boolean interruptedByError = false;
        private boolean interruptedByTimeOut = false;
        private boolean isActive = true;

        public Semaphore(String info) {
            this.semaphoreInfo = info;
        }

        public CallbackResult waitForCallback() {
            return waitForCallback(null);
        }

        /**
         * Waits until Communication class calls the callback method.
         *
         * @param timeout Maximum time in millis to wait.
         *                If null or value = 0 there is no maximum time, means the function doesn't
         *                returns until the callback is called
         * @return
         */
        public CallbackResult waitForCallback(Long timeout) {
            if (null == timeout) {
                timeout = new Long(0);
            }

            long timeToStop = System.currentTimeMillis() + timeout;
            while (isActive && !(interruptedByTimeOut || interruptedByUser || interruptedByError)) {
                try {
                    Thread.sleep(100);
                    if (timeout > 0 && System.currentTimeMillis() > timeToStop) {
                        interruptedByTimeOut = true;
                        break;
                    }
                } catch (InterruptedException ie) {
                }
            }

            isActive = false;
            if (interruptedByTimeOut) return CallbackResult.TIMEOUT;
            if (interruptedByError) return CallbackResult.INVALID_OPERATION;
            if (interruptedByUser) return CallbackResult.OPERATION_CANCELED;
            return callbackResult;
        }

        public boolean isWaiting() {
            return isActive && !(interruptedByError || interruptedByUser || interruptedByTimeOut);
        }

        public void update(int requestId, CallbackResult callbackResult) {
            this.requestId = requestId;
            this.callbackResult = callbackResult;
        }

        public void deactivate() {
            this.isActive = false;
        }

        public void interrruptByUser() {
            this.interruptedByUser = true;
        }

        public void interruptByTimeout() {
            this.interruptedByTimeOut = true;
        }

        public void interruptByError() {
            this.interruptedByError = true;
        }

        public String getSemaphoreInfo() {
            return semaphoreInfo;
        }
    }
}

