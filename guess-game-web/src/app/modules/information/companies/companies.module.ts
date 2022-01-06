import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { GeneralModule } from '../../general/general.module';
import { InformationModule } from '../information.module';
import { MessageModule } from '../../message/message.module';
import { CompanyComponent } from './company.component';
import { CompaniesListComponent } from './companies-list.component';
import { CompaniesSearchComponent } from './companies-search.component';
import { CompaniesSwitcherComponent } from './companies-switcher.component';

@NgModule({
  declarations: [
    CompanyComponent,
    CompaniesListComponent,
    CompaniesSearchComponent,
    CompaniesSwitcherComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule,
    GeneralModule,
    InformationModule,
    MessageModule,
    TranslateModule
  ]
})
export class CompaniesModule {
}
