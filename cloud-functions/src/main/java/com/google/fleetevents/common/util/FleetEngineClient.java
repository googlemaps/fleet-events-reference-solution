package com.google.fleetevents.common.util;

import com.google.fleetengine.auth.AuthTokenMinter;
import com.google.fleetengine.auth.client.FleetEngineAuthClientInterceptor;
import com.google.fleetengine.auth.client.FleetEngineTokenProvider;
import com.google.fleetengine.auth.token.FleetEngineToken;
import com.google.fleetengine.auth.token.factory.signer.ImpersonatedSigner;
import com.google.fleetengine.auth.token.factory.signer.SignerInitializationException;
import com.google.fleetengine.auth.token.factory.signer.SigningTokenException;
import com.google.fleetevents.lmfs.config.FleetEventConfig;
import google.maps.fleetengine.delivery.v1.DeliveryServiceGrpc;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import google.maps.fleetengine.delivery.v1.GetDeliveryVehicleRequest;
import google.maps.fleetengine.delivery.v1.GetTaskRequest;
import google.maps.fleetengine.delivery.v1.Task;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client class to create requests to Fleet Engine to retrieve latest state for task and delivery
 * vehicles.
 */
public class FleetEngineClient {

  private static final Logger logger = Logger.getLogger(FleetEngineClient.class.getName());
  private final ServerTokenProvider fleetEngineTokenProvider;
  private final String projectId;

  public FleetEngineClient() throws SignerInitializationException {
    this.projectId = FleetEventConfig.getProjectId();
    this.fleetEngineTokenProvider = new ServerTokenProvider(createMinter());
  }

  public Optional<Task> getTask(String taskId) {
    var channel = getChannel();
    var stub = DeliveryServiceGrpc.newBlockingStub(channel);

    String taskName = getTaskName(taskId);
    GetTaskRequest getTaskRequest = GetTaskRequest.newBuilder().setName(taskName).build();

    Optional<Task> optionalTask;
    try {
      Task task = stub.getTask(getTaskRequest);
      optionalTask = Optional.of(task);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "Attempted to retrieve entity, but encountered error", e);
      optionalTask = Optional.empty();
    }
    channel.shutdown();
    return optionalTask;
  }

  public Optional<DeliveryVehicle> getDeliveryVehicle(String deliveryVehicleId) {
    var channel = getChannel();
    var stub = DeliveryServiceGrpc.newBlockingStub(channel);

    String deliveryVehicleName = getDeliveryVehicleName(deliveryVehicleId);
    GetDeliveryVehicleRequest getDeliveryVehicleRequest =
        GetDeliveryVehicleRequest.newBuilder().setName(deliveryVehicleName).build();

    Optional<DeliveryVehicle> optionalDeliveryVehicle;
    try {
      DeliveryVehicle deliveryVehicle = stub.getDeliveryVehicle(getDeliveryVehicleRequest);
      optionalDeliveryVehicle = Optional.of(deliveryVehicle);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "Attempted to retrieve entity, but encountered error", e);
      optionalDeliveryVehicle = Optional.empty();
    }
    channel.shutdown();
    return optionalDeliveryVehicle;
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
        Metadata.Key.of("google-cloud-resource-prefix", Metadata.ASCII_STRING_MARSHALLER),
        String.format("providers/%s", projectId));

    return ManagedChannelBuilder.forTarget(FleetEventConfig.getFleetEngineEndpoint())
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
