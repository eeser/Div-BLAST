function setSplit(){
try{
	var A=arguments[0];
	var j=arguments[1];
	var Y=arguments[2];
	// alert("elif");
								// real interval, interval number, the
// length will be split for seq //
// yourImg=document.getElementById('scal');
	var index=2; var white=A-2;
	
	var row=document.getElementById('split');
	var interval=Y/j; 
	var previous=interval;// ilk i icin bi sýkntý var
	var image; 
	var tempw; 
	var c0;

	for(var i=1;i<=j;i++){
		
		// white add
		image=document.createElement('img');
		image.src="./image_file/white.gif";
		image.style.height='4px';
		image.style.width=white+'px';
		c0=row.insertCell(index);
		c0.appendChild(image);// white add
		
		index++;
		
		// split add
		
		image=document.createElement('img');
		image.src="./image_file/scale.gif";
		image.style.height='10px';
		image.style.width='2px';
		c0=row.insertCell(index);
		c0.appendChild(image);// white add
		
		index++;
	}

	index=2;
	row=document.getElementById('splitNo');

	for(var i=1;i<=j;i++){
		tempw=white-(digitNumber(previous-interval))*6;// burdaki 2 deil
		// basamak sayýsý olacak previousýn ama ilk
		// previous icin ozel duruum
		image=document.createElement('img');
		image.src="./image_file/white.gif";
		image.style.height='4px';
		image.style.width=tempw+'px';
		c0=row.insertCell(index);
		c0.appendChild(image);// white add
		
		index++;
		c0=row.insertCell(index);
		c0.innerHTML=previous+"";
	// c0.innerHTML.label.setWidth('10');
		// c0.innerHTML.label.setHeight('4');
		
		previous=previous+interval; 
		index++;
	
	}
}catch(err){
	
}

// renew index

}

function digitNumber(x){
	var count=0;
	var mode=10;
	var remain;
	do{
		remain=x/mode;
		mode=mode*10;
		count++;
	}while(remain>=1);
	
	return count;
}