package it.noxno.hbci.resource;

import it.noxno.hbci.dto.BankAccountDTO;
import it.noxno.hbci.model.BankAccount;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

/**
 * REST resource for managing bank accounts.
 */
@Path("/api/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Bank Accounts", description = "Manage bank accounts for HBCI access")
public class BankAccountResource {

    @GET
    @Operation(summary = "List all bank accounts", description = "Retrieves all configured bank accounts")
    @APIResponse(responseCode = "200", description = "List of bank accounts")
    public List<BankAccount> listAccounts() {
        return BankAccount.listAll();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get bank account by ID", description = "Retrieves a specific bank account")
    @APIResponse(responseCode = "200", description = "Bank account found")
    @APIResponse(responseCode = "404", description = "Bank account not found")
    public Response getAccount(@Parameter(description = "Account ID") @PathParam("id") Long id) {
        BankAccount account = BankAccount.findById(id);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(account).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Create bank account", description = "Creates a new bank account configuration")
    @APIResponse(responseCode = "201", description = "Bank account created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    public Response createAccount(@Valid BankAccountDTO dto) {
        BankAccount account = new BankAccount();
        account.accountNumber = dto.accountNumber;
        account.bankCode = dto.bankCode;
        account.accountHolderName = dto.accountHolderName;
        account.hbciUrl = dto.hbciUrl;
        account.hbciVersion = dto.hbciVersion;
        account.userId = dto.userId;
        account.customerId = dto.customerId;
        account.active = dto.active;
        account.persist();

        return Response.status(Response.Status.CREATED).entity(account).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update bank account", description = "Updates an existing bank account")
    @APIResponse(responseCode = "200", description = "Bank account updated")
    @APIResponse(responseCode = "404", description = "Bank account not found")
    public Response updateAccount(@PathParam("id") Long id, @Valid BankAccountDTO dto) {
        BankAccount account = BankAccount.findById(id);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        account.accountNumber = dto.accountNumber;
        account.bankCode = dto.bankCode;
        account.accountHolderName = dto.accountHolderName;
        account.hbciUrl = dto.hbciUrl;
        account.hbciVersion = dto.hbciVersion;
        account.userId = dto.userId;
        account.customerId = dto.customerId;
        account.active = dto.active;

        return Response.ok(account).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete bank account", description = "Deletes a bank account")
    @APIResponse(responseCode = "204", description = "Bank account deleted")
    @APIResponse(responseCode = "404", description = "Bank account not found")
    public Response deleteAccount(@PathParam("id") Long id) {
        BankAccount account = BankAccount.findById(id);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        account.delete();
        return Response.noContent().build();
    }
}
