// IDashBoardServiceInterface.aidl
package com.atc0194.huge_homework;

// Declare any non-default types here with import statements

import com.atc0194.huge_homework.IDashBoardCallback;

interface IDashBoardServiceInterface {
   void setBusType(String busType);
   String getData();

   void registerCallback(IDashBoardCallback cb);
   void unregisterCallback(IDashBoardCallback cb);
}
