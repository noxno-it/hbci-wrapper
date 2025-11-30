import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="dashboard">
      <h2>Dashboard</h2>
      
      <div class="cards-grid">
        <div class="card" routerLink="/accounts">
          <h3>Bank Accounts</h3>
          <p>Manage your bank accounts and HBCI connections</p>
          <button class="primary">View Accounts</button>
        </div>
        
        <div class="card">
          <h3>Transactions</h3>
          <p>View and fetch transaction history</p>
          <button class="primary">View Transactions</button>
        </div>
        
        <div class="card">
          <h3>Balance</h3>
          <p>Check account balances</p>
          <button class="primary">Check Balance</button>
        </div>
        
        <div class="card">
          <h3>Settings</h3>
          <p>Configure HBCI service settings</p>
          <button class="primary">Settings</button>
        </div>
      </div>

      <div class="card info-section">
        <h3>About HBCI Wrapper</h3>
        <p>
          This microservice provides HBCI/FinTS banking operations through a REST API.
          It wraps the LGPL-licensed hbci4java library with proper license isolation.
        </p>
        <ul>
          <li>✓ Fetch account balances</li>
          <li>✓ Retrieve transaction history</li>
          <li>✓ Manage multiple bank accounts</li>
          <li>✓ OpenAPI/Swagger documentation</li>
          <li>✓ License-safe client integration</li>
        </ul>
      </div>
    </div>
  `,
  styles: [`
    .dashboard {
      padding: 20px 0;
    }
    
    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
    }
    
    .card {
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    
    .card:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }
    
    .card h3 {
      color: #667eea;
      margin-bottom: 10px;
    }
    
    .card p {
      color: #666;
      margin-bottom: 15px;
    }
    
    .info-section {
      cursor: default;
    }
    
    .info-section:hover {
      transform: none;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .info-section ul {
      list-style: none;
      padding-left: 0;
    }
    
    .info-section li {
      padding: 8px 0;
      color: #555;
    }
  `]
})
export class DashboardComponent {}
