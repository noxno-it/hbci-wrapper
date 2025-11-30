import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BankAccount {
  id: number;
  accountNumber: string;
  bankCode: string;
  accountHolderName: string;
  hbciUrl: string;
  hbciVersion: string;
  userId?: string;
  customerId?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BalanceDTO {
  accountId: number;
  bookedBalance: number;
  pendingBalance: number;
  currency: string;
  timestamp: string;
}

export interface TransactionDTO {
  id: number;
  bankAccountId: number;
  valueDate: string;
  bookingDate: string;
  amount: number;
  currency: string;
  purpose?: string;
  otherAccountNumber?: string;
  otherBankCode?: string;
  otherName?: string;
  transactionCode?: string;
}

@Injectable({
  providedIn: 'root'
})
export class HbciService {
  private http = inject(HttpClient);
  private baseUrl = '/api';

  getAccounts(): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(`${this.baseUrl}/accounts`);
  }

  getAccount(id: number): Observable<BankAccount> {
    return this.http.get<BankAccount>(`${this.baseUrl}/accounts/${id}`);
  }

  createAccount(account: Partial<BankAccount>): Observable<BankAccount> {
    return this.http.post<BankAccount>(`${this.baseUrl}/accounts`, account);
  }

  getBalance(accountId: number): Observable<BalanceDTO> {
    return this.http.get<BalanceDTO>(`${this.baseUrl}/hbci/balance/${accountId}`);
  }

  fetchTransactions(accountId: number, startDate: string, endDate: string): Observable<TransactionDTO[]> {
    return this.http.post<TransactionDTO[]>(`${this.baseUrl}/hbci/transactions/fetch`, {
      accountId,
      startDate,
      endDate
    });
  }

  getStoredTransactions(accountId: number, startDate?: string, endDate?: string): Observable<TransactionDTO[]> {
    let url = `${this.baseUrl}/hbci/transactions/${accountId}`;
    if (startDate && endDate) {
      url += `?startDate=${startDate}&endDate=${endDate}`;
    }
    return this.http.get<TransactionDTO[]>(url);
  }
}
