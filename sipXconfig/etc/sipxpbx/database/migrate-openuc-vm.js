
function importVmMetadata(messageId, audioCode, audioType, doc) {
  var audioIdentifier = null;

  if(audioCode == '00') {
    audioIdentifier = 'CURRENT';
  } else if(audioCode == '01') {
    audioIdentifier = 'ORIGINAL';
  } else if(audioCode == 'FW') {
    audioIdentifier = 'COMBINED';
  } else {
    print("Failed to migrate: " + messageId + " - Unknown audio identifier: " + audioCode);
    return;
  }

  var cursor = db.voicemail.metadata.find(
    {
      "user":doc.metadata.user,
      "messageId":messageId,
      "audioIdentifier":audioIdentifier
    }).limit(1);

  var vmMetadata = cursor.hasNext() ? cursor.next() : null;
  if(vmMetadata != null) {
    if(vmMetadata.timestamp > doc.uploadDate.getTime()) {
      print("Ignore duplicate file: " + doc.metadata.user + " - " + doc.filename);
      return;
    } else {
      db.voicemail.files.remove(
        {
          "filename":doc.filename,
          "metadata.voicemailId":vmMetadata._id
        });
      db.voicemail.metadata.remove(vmMetadata);
      print("Replace duplicate file: " + doc.metadata.user + " - " + doc.filename);
    }
  }

  var priority = 'normal';
  if(doc.metadata.urgent != null && doc.metadata.urgent) {
    priority = 'urgent';
  }

  var unheard = false;
  if(doc.metadata.new != null) {
    unheard = doc.metadata.new;
  }

  var label = "deleted"
  if(doc.metadata.folder != null) {
    label = doc.metadata.folder;
  }

  var subject = doc.metadata.msgid;
  if(doc.metadata.subject != null) {
    subject = doc.metadata.subject;
  }

  var otherRecipients = [];
  if(doc.metadata.others != null && doc.metadata.others instanceof Array) {
    otherRecipients = doc.metadata.others;
  }
   
  var metadataId = new ObjectId();
  var metadata = {
    "_id":metadataId,
    "user":doc.metadata.user,
    "label":label,
    "messageId":doc.metadata.msgid,
    "audioIdentifier":audioIdentifier,
    "unheard":unheard,
    "userURI":doc.metadata.id,
    "fromURI":doc.metadata.from,
    "subject":subject,
    "timestamp": new NumberLong(new Date(doc.metadata.timestamp).getTime()),
    "audioFormat":audioType,
    "priority":priority,
    "otherRecipients": otherRecipients
  };

  db.voicemail.metadata.insert(metadata);
  importVmFile(doc, metadata);
}

function importGreetingMetadata(greetingType, audioType, doc) {
  var cursor = db.voicemail.metadata.find(
    {
      "user":doc.metadata.user, 
      "label":"GREETINGS", 
      "messageId":greetingType
    }).limit(1) ;

  var greetingsMetadata = cursor.hasNext() ? cursor.next() : null;
  if(greetingsMetadata != null) {
    if(greetingsMetadata.timestamp > doc.uploadDate.getTime()) {
      print("Ignore duplicate file: " + doc.metadata.user + " - " + doc.filename);
      return;
    } else {
      db.voicemail.files.remove(
        {
          "filename":doc.filename,
          "metadata.voicemailId":greetingsMetadata._id
        });
      db.voicemail.metadata.remove(greetingsMetadata);
      print("Replace duplicate file: " + doc.metadata.user + " - " + doc.filename);
    }
  }

  var metadataId = new ObjectId();
  var metadata = {
      "_id":metadataId,
      "user":doc.metadata.user,
      "label":"GREETINGS",
      "messageId":greetingType,
      "audioIdentifier":"CURRENT",
      "unheard":false,
      "fromURI":doc.metadata.from,
      "timestamp":doc.uploadDate.getTime(),
      "priority":"normal",
      "audioFormat":audioType,
    };

  db.voicemail.metadata.insert(metadata);
  importVmFile(doc, metadata);
}

function importRecorderMetadata(audioType, doc) {
  var cursor = db.voicemail.metadata.find(
    {
      "user":doc.metadata.user, 
      "label":"RECORDER", 
      "messageId":"RECORDER-MSGID"
    }).limit(1) ;

  var recorderMetadata = cursor.hasNext() ? cursor.next() : null;
  if(recorderMetadata != null) {
    if(recorderMetadata.timestamp > doc.uploadDate.getTime()) {
      print("Ignore duplicate file: " + doc.metadata.user + " - " + doc.filename);
      return;
    } else {
      db.voicemail.files.remove(
        {
          "filename":doc.filename,
          "metadata.voicemailId":recorderMetadata._id
        });
      db.voicemail.metadata.remove(recorderMetadata);
      print("Replace duplicate file: " + doc.metadata.user + " - " + doc.filename);
    }
  }

  var metadataId = new ObjectId();
  var metadata = {
      "_id":metadataId,
      "user":doc.metadata.user,
      "label":"RECORDER",
      "messageId":"RECORDER-MSGID",
      "audioIdentifier":"CURRENT",
      "unheard":false,
      "fromURI":doc.metadata.from,
      "timestamp":doc.uploadDate.getTime(),
      "priority":"normal",
      "audioFormat":audioType,
    };

  db.voicemail.metadata.insert(metadata);
  importVmFile(doc, metadata);
}

function importVmFile(doc, metadata) {
  
  var contentType = null;
  if(metadata.audioFormat == "wav") {
    contentType = "audio/wav";
  } else if(metadata.audioFormat == "mp3") {
    contentType = "audio/mpeg";
  }

  var duration = new NumberLong(0);
  if(doc.metadata.durationsecs != null) {
    duration = new NumberLong(doc.metadata.durationsecs);
  }

  var vmFile = {
      "_id": doc._id,
      "chunkSize": doc.chunkSize,
      "length": doc.length,
      "md5": doc.md5,
      "filename": doc.filename,
      "contentType": contentType,
      "uploadDate": doc.uploadDate,
      "aliases": doc.aliases,
      "metadata":{
        "voicemailId":metadata._id,
        "audioIdentifier":metadata.audioIdentifier,
        "duration":duration,
        "timestamp":metadata.timestamp,
        "filePath":null,
        "audioFormat":metadata.audioFormat,
        "contentLength":doc.length
      }
    };

  db.voicemail.files.insert(vmFile);
}

db.voicemail.chunks.createIndex( { "files_id": 1, "n": 1 }, { unique: true } );
db.voicemail.files.createIndex( { "filename": 1, "uploadDate": 1 } );
db.voicemail.files.createIndex( { "metadata.voicemailId": 1} );
db.voicemail.metadata.createIndex( { "user": 1, "label": 1, "messageId": 1 } );

print("Start Migrating VM Metadata Schema.");

db.fs.files.find({}).addOption(DBQuery.Option.noTimeout).forEach( function(doc) {
  print("Migrating VM Metadata Schema -> user: " + doc.metadata.user + " filename: " + doc.filename);
  var audioToken = doc.filename.match('(.*)-([0-9A-Z][0-9A-Z])\.([a-z][a-z][a-z])');
  if(audioToken != null) {
    importVmMetadata(audioToken[1], audioToken[2], audioToken[3], doc);
  } else {
    audioToken = doc.filename.match('(.*)\.([a-z][a-z][a-z])');
    if(audioToken != null) {
      if (['standard', 'outofoffice', 'extendedabs'].indexOf(audioToken[1]) >= 0) {
        importGreetingMetadata(audioToken[1], audioToken[2], doc);
      } else if(audioToken[1] == 'name') {
        importRecorderMetadata(audioToken[2], doc);
      } else {
        print("Unsupported file format: " + doc.filename);  
      }
    } else {
      print("Unsupported file format: " + doc.filename);
    }
  }
});

print("Done Migrating VM Metadata Schema.");
print("Start Migrating VM Chunk Files.");

var voicemailSize = db.voicemail.files.count({});
var percentLog = Math.round(voicemailSize * (2/100));
var counter = 0;

db.voicemail.files.find({}).addOption(DBQuery.Option.noTimeout).forEach( function(doc) {
  var cursor = db.fs.chunks.find({"files_id":doc._id});
  if(cursor.hasNext()) {
    cursor.forEach(function(chunk) {
      db.voicemail.chunks.insert(chunk);
    });
  }

  if((++counter % percentLog) == 0) {
    print("Transfered VM Chunk Files: " + counter);
  }
});

if((counter % percentLog) != 0) {
  print("Transfered VM Chunk Files: " + counter);
}

print("Done Migrating VM Chunk Files.");