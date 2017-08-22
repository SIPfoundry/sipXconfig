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
  if(doc.metadata.urgent) {
    priority = 'urgent'
  }

  var otherRecipients = [];
  if(doc.metadata.others != null && doc.metadata.others instanceof Array) {
    otherRecipients = doc.metadata.others;
  }
   
  var metadataId = new ObjectId();
  var metadata = {
    "_id":metadataId,
    "user":doc.metadata.user,
    "label":doc.metadata.folder,
    "messageId":doc.metadata.msgid,
    "audioIdentifier":audioIdentifier,
    "unheard":doc.metadata.new,
    "userURI":doc.metadata.id,
    "fromURI":doc.metadata.from,
    "subject":doc.metadata.subject,
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

  var vmFile = {
      "_id": doc._id,
      "chunkSize": doc.chunkSize,
      "length": doc.length,
      "md5": doc.mp5,
      "filename": doc.filename,
      "contentType": contentType,
      "uploadDate": doc.uploadDate,
      "aliases": doc.aliases,
      "metadata":{
        "voicemailId":metadata._id,
        "audioIdentifier":metadata.audioIdentifier,
        "duration":doc.metadata.durationsecs != null ? new NumberLong(doc.metadata.durationsecs) : new NumberLong(0),
        "timestamp":metadata.timestamp,
        "filePath":null,
        "audioFormat":metadata.audioFormat,
        "contentLength":doc.length
      }
    };

  db.voicemail.files.insert(vmFile);
  importVmChunk(doc, metadata);
}

function importVmChunk(doc, metadata) {
  var cursor = db.fs.chunks.find({"files_id":doc._id});
  if(cursor.hasNext()) {
    db.voicemail.chunks.insert(cursor.toArray());
  } else {
    print("Unable to locate chunk file for: " + doc._id + ": " + metadata.messageId);
    db.voicemail.files.remove({"_id": doc._id});
    db.voicemail.metadata.remove({"_id": metadata._id});
  }
}

db.voicemail.chunks.createIndex( { "files_id": 1, "n": 1 }, { unique: true } );
db.voicemail.files.createIndex( { "filename": 1, "uploadDate": 1 } );
db.voicemail.files.createIndex( { "metadata.voicemailId": 1} );
db.voicemail.metadata.createIndex( { "user": 1, "label": 1, "messageId": 1 } );

db.fs.files.find({}).forEach( function(doc) {
  print("Migrating user: " + doc.metadata.user + " filename: " + doc.filename);
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