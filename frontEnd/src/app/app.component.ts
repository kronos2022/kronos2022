import { formatDate } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import * as JSZip from 'jszip';
import { AppService } from './app.service';
let zipFile: JSZip = new JSZip();

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})


export class AppComponent {
  title = 'frontEnd';
  fileValue:any;
  myForm = new FormGroup({
    file: new FormControl('', [Validators.required])
  });
  fileBlob: any;
  constructor(private fileService: AppService) { 

  }

  fileChanged(e: any){
    this.fileValue = e.target.files[0];
  }
  submit(){
    if(this.fileValue.type == "zip" || this.fileValue.type == "application/zip" || this.fileValue.type == "application/x-zip" || this.fileValue.type == "application/x-zip-compressed"){
     var fileArrVal: any[] = [] 
      JSZip.loadAsync(this.fileValue).then((zip) => {
        var fileArray = Object.entries(zip.files)
        fileArray.forEach(([key, value]) => {
          fileArrVal.push(value)
        });
        fileArrVal.forEach((val)=>{
           const formData = new FormData()
          formData.append("multipartFile",val as Blob);
          this.fileService.passFile(formData).subscribe((data: any) =>{
            console.log(data)
          });
        })
      })
    }else{
      const formData = new FormData()
      formData.append("multipartFile",this.fileValue, this.fileValue.name);
      this.fileService.passFile(formData).subscribe((data: any) =>{
        console.log(data)
      });
    
  }
    }
}

