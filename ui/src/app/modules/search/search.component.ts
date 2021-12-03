import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SearchService } from 'src/app/services/search/search.service';
import { Configs } from 'src/app/_configs/configs';
import { Result } from 'src/app/_shared/models/result';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.scss']
})
export class SearchComponent implements OnInit {

  constructor(private _fb: FormBuilder, private _search: SearchService) { }

  // defining variables
  loading: boolean = false;
  result: Result[] = [];
  logo = "../../../assets/logo.png";
  form: FormGroup = this._fb.group({
    searchText: new FormControl('', Validators.required)
  })
  
  ngOnInit(): void {}

  // getting each letter/character typed by the user and fetching the corresponding data via the api
  async onKey(){
    let text = this.form.get('searchText')?.value;
    await this.fetchData(text);
    this.loading = true;
    if(text == ""){
      this.result = [];
      this.loading = false;
    }
  }

  // fetching data from the api and storing them to be displayed
  async fetchData(value: String) {
    const trimVal = value.trim().toLocaleLowerCase();
    if (trimVal.length > 0) {  
      let x = this._search.get(trimVal);
      this.result = [];
      x.forEach(s=>{
        this.result.push(JSON.parse(JSON.stringify(s)) as Result);
        this.loading = false;
      })
    }
  }
}
