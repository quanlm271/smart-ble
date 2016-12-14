var convert= require('convert-hex')
module.exports ={
	
// PIN HASH: 38515351395153513551535131515351
// PIN DATA: 00A138393531C4A7C46353536C316543
// ENC: 85B59B6F699BF920D6C1DF1A9674591D
// PreHash: 85B59B6F699BF920D6C1DF1A9674591D
// PHASH: 85B59B6F699BF920D6C1DF1A9674591D88C25512345A0010

	create_pin_hash: function(stringHexPin){
		var bytePinHash = new Uint8Array(16);
		var bytePIN = convert.hexToBytes(stringHexPin);
		for(var i=0; i<4; i++ ){
            bytePinHash[i*4]=bytePIN[i];
        }
        for(var i=0; i<16; i++){
            if(i%4 != 0){
                if(i%2 != 0)
                    bytePinHash[i] = 0x51; //Q
                else
                    bytePinHash[i] = 0x53; //S
            }
        }
       	console.log("PIN HASH: ",convert.bytesToHex(bytePinHash));
       	return bytePinHash;
	},

	//mac, uid in pindata from app
	create_command_hash: function(pinData, userType, mac, uid, masterKey){
		var byteCommandData = new Uint8Array(21);
		var byte_pinData = convert.hexToBytes(pinData);
		var byte_userType = convert.hexToBytes(userType);
		var byte_uid = convert.hexToBytes(uid);

		//var byte_mac = convert_macToBytes(mac);
		var byte_mac = convert.hexToBytes(mac);

		var index = 0;
		//12
		for(var i=0; i<byte_pinData.length; i++){
			if( i<2 || i>5)//remove 4 bytes pin
				byteCommandData[index++]= byte_pinData[i];
		}
		console.log("index",index);
				console.log(convert.bytesToHex(byteCommandData))
		//1
		byteCommandData[index++]=byte_userType[0];
		console.log("",index);
		console.log("",convert.bytesToHex(byteCommandData))
		//6
		for(var i=0; i<6; i++){
			byteCommandData[index++]= byte_mac[i];
		}
		console.log("",index);
		console.log("",convert.bytesToHex(byteCommandData))
		//2
		for(var i=0; i<2; i++){
			byteCommandData[index++]= byte_uid[i];
		}
		console.log(byteCommandData)
	},
	
	convert_macToBytes:function(mac){
		return convert.hexToBytes(mac.replace(':',''));
	},

	create_random_int:function(range) {
		return Math.floor((Math.random() * range) + 1);
	},

	convert_uidToBytes:function(uid){
		var s = s1 + s2;
		var r = conv.hexToBytes(s);
		console.dir((r[1] & 0xFF) |
		            ((r[0] & 0xFF) << 8));
	},

}