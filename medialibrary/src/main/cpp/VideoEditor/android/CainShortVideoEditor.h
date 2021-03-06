//
// Created by CainHuang on 2019/2/17.
//

#ifndef CAINSHORTVIDEOEDITOR_H
#define CAINSHORTVIDEOEDITOR_H

#include <Thread.h>
#include <Mutex.h>
#include <Condition.h>

enum editor_event_type {
    EDITOR_PROCESSING = 1,
    EDITOR_ERROR = 100,
};

class ShortVideoEditorListener {
public:
    virtual void notify(int msg, int ext1, int ext2, void *obj) {}
};

typedef struct Message {
    int what;
    int arg1;
    int arg2;
    void *obj;
    void (*free)(void *obj);
    struct Message *next;
} Message;

class CainShortVideoEditor : public Runnable {
public:
    CainShortVideoEditor();

    virtual ~CainShortVideoEditor();

    void init();

    void disconnect();

    void setListener(ShortVideoEditorListener *listener);

    int execute(int argc, char **argv);

    void postMessage(int what, int arg1 = 0, int arg2 = 0, void *obj = NULL, int len = 0);

protected:
    void run() override;

private:
    int getMessage(Message *msg);

private:
    Mutex mMutex;
    Condition mCondition;
    Thread *msgThread;
    bool abortRequest;
    ShortVideoEditorListener *mListener;
    Message *mFirstMsg, *mLastMsg;
    int mSize;
};

#endif //CAINSHORTVIDEOEDITOR_H
