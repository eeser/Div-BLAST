function setAlignment(){//ilk index var
	try{
		
	table=document.createElement('table');
	aligntable=document.getElementById("aligntable");
	table.border="0px";
	table.cellPadding="0px";
	table.cellSpacing="0px";

	var index=arguments[0]; 
	var row=table.insertRow(index); 
	var cindex=1;

	var link=document.createElement('a');
	link.href="#seqDetail";
	var c0; 
	var image=document.createElement('img');
	image.src="image_file/white.gif";
	image.style.height='4px';
	image.style.width='50px';
	c0=row.insertCell(0);
	c0.appendChild(image); 
	var i;
	for(i=1;i<arguments.length-2;i++){
		if(i%2==1){
			image=document.createElement('img');
			image.style.height='4px';
			image.src="image_file/"+arguments[i]+".gif";
		}

		else{ 
			c0=row.insertCell(cindex);
			image.style.width=arguments[i]+"px";
			c0.appendChild(image);

			cindex++; 
		} 
	}

	//image.src='./result_files/white.gif';

	link.appendChild(table);

	alignTable.appendChild(link);

	;
	row.title=arguments[i];
	row.value=arguments[i+1];
	row.onclick=function(){
		
		setLabel(this.value);
		//alert(this.value);
		
	//	setLabel(arguments[i]);
	};
	//new table
	table=document.createElement('table');
	table.border="0px";
	table.cellPadding="0px";
	table.cellSpacing="0px";
	row=table.insertRow(0);
	
	image=document.createElement('img');
	image.src="image_file/white.gif";
	image.style.height='4px';
	image.style.width='550px';
	c0=row.insertCell(0);
	c0.appendChild(image);
	alignTable.appendChild(table);
	
	
	
	}catch(err){
		
	}


}

function ignoreerror()
{
	console.log("dfrghgh");
   return true;
}

function errMs(){
	window.onerror=function(){return true;};
}

function setLabel(l){
//	alert(l);
//	var x="#,,,,"
	var t=l.replace(/#/g,"<br/>");
	var k=t.replace(/ /g,"&nbsp;");
//	\u00A0
	//&#8198;&#8198;&#8198;
	//var h=k.replace(/\t/g,"&nbsp;&nbsp;&nbsp;&nbsp;");
	seqDetail.innerHTML=k;
	
	
	//$('seqDetail').label=l;
	
	
}

function setLabel2(l,t){
//	alert(seqDetail.value);
	$('seqDetail').value=l;
	
	
}