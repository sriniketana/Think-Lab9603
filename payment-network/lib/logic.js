'use strict';
/**
 * Write your transction processor functions here
 */

/**
 * Sample transaction
 * @param {com.ibm.payments.TransferMoney} transferMoney
 * @transaction
 */
function onTransferMoney(transferMoney) {
    var assetRegistry;
    var sourceId = transferMoney.source.accountId; 
    var destId = transferMoney.destination.accountId; 
    var amount = transferMoney.amount; 
    console.log("source id is " + sourceId);
    // transferMoney.source.balance = transferMoney.source.balance - amount; 
    // transferMoney.destination.balance = transferMoney.destination.balance + amount; 
    return getAssetRegistry('com.ibm.payments.Account')
        .then(function(ar) {
            assetRegistry = ar;
            assetRegistry.get(sourceId)
            .then(function(asset) {
                asset.balance = asset.balance - amount;
                return assetRegistry.update(asset);
            })
            .then(function(){
                assetRegistry.get(destId)
                .then(function(asset) {
                    asset.balance =asset.balance + amount; 
                    return assetRegistry.update(asset);
                });
            })
        })
}