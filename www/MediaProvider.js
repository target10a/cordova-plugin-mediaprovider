var exec = require('cordova/exec');


module.exports = {
    /**
     * Reads text of a content media provider url like content://com.whatsapp.provider.media/item/fa1db2f2-2869-467c-81d9-e2e80d7ad4a3.
     * @param {String} path  Content URL/path.
     * @param successCallback  invoked with a native filesystem path string
     * @param errorCallback  invoked if error occurs
     */
    readText: function (path, successCallback, errorCallback) {
        exec(successCallback, errorCallback, "MediaProvider", "readText", [path]);
    }
};
