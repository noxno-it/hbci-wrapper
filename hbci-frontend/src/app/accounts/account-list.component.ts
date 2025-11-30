import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HbciService } from '../services/hbci.service';

@Component({
  selector: 'app-account-list',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="account-list">
      <div class="header">
        <h2>Bank Accounts</h2>
        <button class="primary" (click)="loadAccounts()">Refresh</button>
      </div>

      <div class="card" *ngIf="loading">
        <p>Loading accounts...</p>
      </div>

      <div class="card" *ngIf="error">
        <p style="color: red;">{{ error }}</p>
      </div>

      <div class="card" *ngIf="!loading && accounts.length === 0">
        <p>No bank accounts configured yet.</p>
        <p>Add your first account to get started with HBCI operations.</p>
      </div>

      <div class="card" *ngIf="!loading && accounts.length > 0">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Account Number</th>
              <th>Bank Code</th>
              <th>Holder Name</th>
              <th>HBCI Version</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let account of accounts">
              <td>{{ account.id }}</td>
              <td>{{ account.accountNumber }}</td>
              <td>{{ account.bankCode }}</td>
              <td>{{ account.accountHolderName }}</td>
              <td>{{ account.hbciVersion }}</td>
              <td>
                <span [class.active]="account.active" [class.inactive]="!account.active">
                  {{ account.active ? 'Active' : 'Inactive' }}
                </span>
              </td>
              <td>
                <button (click)="viewBalance(account.id)">Balance</button>
                <button (click)="viewTransactions(account.id)">Transactions</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `,
  styles: [`
    .account-list {
      padding: 20px 0;
    }
    
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }
    
    .active {
      color: green;
      font-weight: 600;
    }
    
    .inactive {
      color: red;
      font-weight: 600;
    }
    
    button {
      margin-right: 8px;
      padding: 6px 12px;
      font-size: 12px;
    }
    
    table {
      font-size: 14px;
    }
  `]
})
export class AccountListComponent implements OnInit {
  private hbciService = inject(HbciService);
  
  accounts: any[] = [];
  loading = false;
  error: string | null = null;

  ngOnInit() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.loading = true;
    this.error = null;
    
    this.hbciService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load accounts: ' + err.message;
        this.loading = false;
      }
    });
  }

  viewBalance(accountId: number) {
    alert('Balance view for account ' + accountId + ' - To be implemented');
  }

  viewTransactions(accountId: number) {
    alert('Transactions view for account ' + accountId + ' - To be implemented');
  }
}
