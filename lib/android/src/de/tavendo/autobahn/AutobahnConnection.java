/******************************************************************************
 *
 *  Copyright 2011 Tavendo GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package de.tavendo.autobahn;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.jackson.type.TypeReference;

import de.tavendo.autobahn.Autobahn.OnCallResult;

public class AutobahnConnection extends WebSocketConnection {

   protected AutobahnWriter mWriterHandler;

   private final Random mRng = new Random();

   private static final char[] mBase64Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
         .toCharArray();

   private static class CallResultMeta {

      CallResultMeta(OnCallResult handler, Class<?> resultClass) {
         this.mResultHandler = handler;
         this.mResultClass = resultClass;
         this.mResultTypeRef = null;
      }

      CallResultMeta(OnCallResult handler, TypeReference<?> resultTypeReference) {
         this.mResultHandler = handler;
         this.mResultClass = null;
         this.mResultTypeRef = resultTypeReference;
      }

      public OnCallResult mResultHandler;
      public Class<?> mResultClass;
      public TypeReference<?> mResultTypeRef;
   }

   private final ConcurrentHashMap<String, CallResultMeta> mCalls = new ConcurrentHashMap<String, CallResultMeta>();

   private OnSession mSessionHandler;

   public AutobahnConnection() {
   }

   /**
    * Create new random ID, i.e. for use in RPC calls to correlate
    * call message with result message.
    */
   private String newId(int len) {
      char[] buffer = new char[len];
      for (int i = 0; i < len; i++) {
         buffer[i] = mBase64Chars[mRng.nextInt(mBase64Chars.length)];
      }
      return new String(buffer);
   }

   /**
    * Create new random ID of default length.
    */
   private String newId() {
      return newId(8);
   }


   public interface OnSession {

      public void onOpen();

      public void onClose();

   }

   public void connect(String wsUri, OnSession sessionHandler) {

      mSessionHandler = sessionHandler;
   }

   public void disconnect() {

   }

   public void call(String procUri, CallResultMeta resultMeta, Object... arguments) {

      AutobahnMessage.Call call = new AutobahnMessage.Call(newId(), procUri, arguments.length);
      for (int i = 0; i < arguments.length; ++i) {
         call.mArgs[i] = arguments[i];
      }

      mCalls.put(call.mCallId, resultMeta);

      //mWriterHandler.forward(call);
   }

   public void call(String procUri, Class<?> resultType, OnCallResult resultHandler, Object... arguments) {

      call(procUri, new CallResultMeta(resultHandler, resultType), arguments);
   }

   public void call(String procUri, TypeReference<?> resultType, OnCallResult resultHandler, Object... arguments) {

      call(procUri, new CallResultMeta(resultHandler, resultType), arguments);
   }

}