package com.google.fleetevents.util;

import com.google.fleetengine.auth.AuthTokenMinter;
import com.google.fleetengine.auth.client.FleetEngineAuthClientInterceptor;
import com.google.fleetengine.auth.client.FleetEngineTokenProvider;
import com.google.fleetengine.auth.token.FleetEngineToken;
import com.google.fleetengine.auth.token.factory.signer.ImpersonatedSigner;
import com.google.fleetengine.auth.token.factory.signer.SignerInitializationException;
import com.google.fleetengine.auth.token.factory.signer.SigningTokenException;
import com.google.fleetevents.config.FleetEventConfig;
import google.maps.fleetengine.delivery.v1.DeliveryServiceGrpc;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import google.maps.fleetengine.delivery.v1.GetDeliveryVehicleRequest;
import google.maps.fleetengine.delivery.v1.GetTaskRequest;
import google.maps.fleetengine.delivery.v1.Task;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

/**
 * Client class to create requests to Fleet Engine to retrieve latest state for task and delivery
 * vehicles.
 */
public class FleetEngineClient {

  private final ServerTokenProvider fleetEngineTokenProvider;
  private final String projectId;

  public FleetEngineClient() throws SignerInitializationException {
    this.projectId = FleetEventConfig.getProjectId();
    this.fleetEngineTokenProvider = new ServerTokenProvider(createMinter());
  }

  public Task getTask(String taskId) {
    var channel = getChannel();
    DeliveryServiceGrpc.DeliveryServiceBlockingStub stub = DeliveryServiceGrpc.newBlockingStub(
        channel);
    String taskName = getTaskName(taskId);

    GetTaskRequest getTaskRequest = GetTaskRequest.newBuilder()
        .setName(taskName)
        .build();

    Task task = stub.getTask(getTaskRequest);
    channel.shutdown();
    return task;
  }

  public DeliveryVehicle getDeliveryVehicle(String deliveryVehicleId) {

    String deliveryVehicleName = getDeliveryVehicleName(deliveryVehicleId);

    GetDeliveryVehicleRequest getDeliveryVehicleRequest = GetDeliveryVehicleRequest.newBuilder()
        .setName(deliveryVehicleName)
        .build();
    var channel = getChannel();
    var stub = DeliveryServiceGrpc.newBlockingStub(
        channel);
    DeliveryVehicle deliveryVehicle = stub.getDeliveryVehicle(getDeliveryVehicleRequest);
    channel.shutdown();
    return deliveryVehicle;
  }

  private String getTaskName(String taskId) {
    return String.format("providers/%s/tasks/%s", projectId, taskId);
  }

  private String getDeliveryVehicleName(String deliveryVehicleId) {
    return String.format("providers/%s/deliveryVehicles/%s", projectId, deliveryVehicleId);
  }

  private AuthTokenMinter createMinter() throws SignerInitializationException {
    return AuthTokenMinter.deliveryBuilder()
        .setDeliveryFleetReaderSigner(
            ImpersonatedSigner.create(FleetEventConfig.getFleetEngineServiceAccountName()))
        .build();
  }

  private ManagedChannel getChannel() {
    Metadata headers = new Metadata();
    headers.put(
        Metadata.Key.of(
            "google-cloud-resource-prefix", Metadata.ASCII_STRING_MARSHALLER),
        String.format("providers/%s", projectId));

    return ManagedChannelBuilder.forTarget(
            FleetEventConfig.getFleetEngineEndpoint())
        .intercept(
            FleetEngineAuthClientInterceptor.create(fleetEngineTokenProvider),
            MetadataUtils.newAttachHeadersInterceptor(headers))
        .build();
  }

  private record ServerTokenProvider(AuthTokenMinter minter) implements FleetEngineTokenProvider {

    @Override
    public FleetEngineToken getSignedToken() throws SigningTokenException {
      return minter.getDeliveryFleetReaderToken();
    }
  }
}
