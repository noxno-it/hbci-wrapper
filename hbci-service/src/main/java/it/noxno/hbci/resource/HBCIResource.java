package it.noxno.hbci.resource;

import it.noxno.hbci.dto.BalanceDTO;
import it.noxno.hbci.dto.TransactionDTO;
import it.noxno.hbci.dto.TransactionRequestDTO;
import it.noxno.hbci.model.BankAccount;
import it.noxno.hbci.model.Transaction;
import it.noxno.hbci.service.HBCIService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

/**
 * REST resource for HBCI banking operations.
 * Requires authentication via Microsoft Entra ID.
 */
@Path("/api/hbci")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "HBCI Operations", description = "Banking operations via HBCI/FinTS")
@SecurityRequirement(name = "oauth2")
@RolesAllowed({"user", "admin"})
public class HBCIResource {

    @Inject
    HBCIService hbciService;

    @GET
    @Path("/balance/{accountId}")
    @Operation(summary = "Get account balance", 
               description = "Fetches the current balance of a bank account via HBCI")
    @APIResponse(responseCode = "200", description = "Balance retrieved successfully")
    @APIResponse(responseCode = "404", description = "Account not found")
    @APIResponse(responseCode = "500", description = "HBCI communication error")
    public Response getBalance(@Parameter(description = "Account ID") @PathParam("accountId") Long accountId) {
        BankAccount account = BankAccount.findById(accountId);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Account not found")
                          .build();
        }

        try {
            BalanceDTO balance = hbciService.getBalance(account);
            return Response.ok(balance).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Failed to fetch balance: " + e.getMessage())
                          .build();
        }
    }

    @POST
    @Path("/transactions/fetch")
    @Operation(summary = "Fetch transactions", 
               description = "Fetches transactions for an account via HBCI for a given date range")
    @APIResponse(responseCode = "200", description = "Transactions retrieved successfully")
    @APIResponse(responseCode = "404", description = "Account not found")
    @APIResponse(responseCode = "500", description = "HBCI communication error")
    public Response fetchTransactions(@Valid TransactionRequestDTO request) {
        BankAccount account = BankAccount.findById(request.accountId);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                          .entity("Account not found")
                          .build();
        }

        try {
            List<TransactionDTO> transactions = hbciService.getTransactions(
                account, request.startDate, request.endDate
            );
            return Response.ok(transactions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Failed to fetch transactions: " + e.getMessage())
                          .build();
        }
    }

    @GET
    @Path("/transactions/{accountId}")
    @Operation(summary = "Get stored transactions", 
               description = "Retrieves transactions stored in the database for an account")
    @APIResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public Response getStoredTransactions(
            @PathParam("accountId") Long accountId,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
        
        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = Transaction.list(
                "bankAccount.id = ?1 and valueDate >= ?2 and valueDate <= ?3 order by valueDate desc",
                accountId, LocalDate.parse(startDate), LocalDate.parse(endDate)
            );
        } else {
            transactions = Transaction.list(
                "bankAccount.id = ?1 order by valueDate desc",
                accountId
            );
        }

        return Response.ok(transactions).build();
    }
}
