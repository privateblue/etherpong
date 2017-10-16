const fs = require('fs');
const solc = require('solc')
const Web3 = require('web3');
const web3 = new Web3();

web3.setProvider(new web3.providers.HttpProvider('http://localhost:8545'));

const owner = web3.eth.accounts[0];

const code = fs.readFileSync('src/main/solidity/Pong.sol').toString();
const compiledCode = solc.compile(code);

const abiDefinition = JSON.parse(compiledCode.contracts[':Pong'].interface);
const byteCode = compiledCode.contracts[':Pong'].bytecode;

const PongContract = web3.eth.contract(abiDefinition);

PongContract.new(800,600,80,15,15,1,3,0,{
  data: byteCode,
  from: owner,
  gas: 4700000}, (err, left) => {
    if (left.side !== undefined) {
      console.log("Left address: " + left.address);
      PongContract.new(800,600,80,15,15,1,3,1,{
        data: byteCode,
        from: owner,
        gas: 4700000}, (err, right) => {
          if (right.side !== undefined) {
            console.log("Right address: " + right.address);
            left.introduce(right.address, {from: owner, gas: 4700000}, function() {
              console.log("Right introduced to Left");
              right.introduce(left.address, {from: owner, gas: 4700000}, function() {
                console.log("Left introduced to Right");
                setInterval(function() {
                  console.log("Left running: " + left.running().toString());
                  console.log("Right running: " + right.running().toString());
                  console.log("Left ball position: " + left.ballPos().toString());
                  console.log("Right ball position: " + right.ballPos().toString());
                  console.log("Updating...")
                  left.update({from: owner, gas: 4700000});
                }, 10000);
              });
            });
          }
        });
    }
  });
