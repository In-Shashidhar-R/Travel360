import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';


import { CommonModule } from '@angular/common';
import { AuditLogResponseDTO } from '../models/compliance.model';
import { ComplianceService } from '../../../core/services/compliance';
import { AuditLog } from '../../admin/compliance/audit-log/audit-log';


@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, FormsModule, AuditLog],
  templateUrl: './audit-logs.html'
})
export class AuditLogsComponent {

}