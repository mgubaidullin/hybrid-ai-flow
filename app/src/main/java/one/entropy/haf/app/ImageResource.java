package one.entropy.haf.app;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Path("/image")
public class ImageResource {

    @Inject
    Logger log;

    @ConfigProperty(name = "image.bucket")
    String bucket;

    @Inject
    S3Client s3;

    @GET
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Images",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON))})
    @Operation(summary = "Get images")
    public List<ImageDto> getImages() throws ExecutionException, InterruptedException, IOException {
        ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder().bucket(bucket).build();
        ListObjectsResponse r = s3.listObjects(listObjectsRequest);
        return r.contents().stream()
                .map(o -> new ImageDto(o.key(), o.size(), o.eTag(), "/image/download/"+o.key(),
                        ZonedDateTime.ofInstant(o.lastModified(), ZoneOffset.UTC)))
                .sorted((o1, o2) -> o1.getLastModified().isAfter(o2.getLastModified()) ? 1 : 0 )
                .collect(Collectors.toList());
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(@MultipartForm FormData formData) throws Exception {
        if (formData.fileName == null || formData.fileName.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (formData.mimeType == null || formData.mimeType.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        PutObjectResponse putResponse = s3.putObject(buildPutRequest(formData), RequestBody.fromBytes(formData.data.readAllBytes()));
        if (putResponse != null) {
            log.infof("Stored to s3: %s", putResponse.toString());
            return Response.ok().status(Response.Status.CREATED).build();
        } else {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/download/{objectKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("objectKey") String objectKey) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GetObjectResponse object = s3.getObject(buildGetRequest(objectKey), ResponseTransformer.toOutputStream(baos));

        Response.ResponseBuilder response = Response.ok((StreamingOutput) output -> baos.writeTo(output));
        response.header("Content-Disposition", "attachment;filename=" + objectKey);
        response.header("Content-Type", object.contentType());
        return response.build();
    }

    protected GetObjectRequest buildGetRequest(String objectKey) {
        return GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
    }

    protected PutObjectRequest buildPutRequest(FormData formData) {
        return PutObjectRequest.builder()
                .bucket(bucket)
                .key(formData.fileName)
                .contentType(formData.mimeType)
                .build();
    }
}