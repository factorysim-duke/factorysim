package edu.duke.ece651.factorysim;

public interface DeliveryListener {
  void onDeliveryAdded(Delivery delivery);
  void onDeliveryFinished(Delivery delivery);
}
