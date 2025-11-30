import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="app">
      <header>
        <div class="container">
          <h1>HBCI Wrapper</h1>
          <p>Banking Operations Management</p>
        </div>
      </header>
      <main class="container">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 30px 0;
      margin-bottom: 30px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    
    header h1 {
      margin: 0;
      font-size: 2rem;
    }
    
    header p {
      margin: 5px 0 0;
      opacity: 0.9;
    }
  `]
})
export class AppComponent {
  title = 'HBCI Wrapper';
}
