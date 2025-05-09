package com.ucentral.rabbitmq_app.model;

public enum RoomType {
   SENCILLA("sencilla"),
   DOBLE("doble"),
   SUITE("suite");

   private final String displayName;

   RoomType(String displayName) {
      this.displayName = displayName;
   }

   public String getDisplayName() {
      return displayName;
   }

   @Override
   public String toString() {
      return displayName;
   }
}
