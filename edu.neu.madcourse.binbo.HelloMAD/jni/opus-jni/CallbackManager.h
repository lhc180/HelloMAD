//
//  CallbackManager.h
//  QVOD
//
//  Created by bigbug on 11-11-21.
//  Copyright (c) 2011年 qvod. All rights reserved.
//

#ifndef QVOD_CallbackManager_h
#define QVOD_CallbackManager_h

//#include <iostream>
//#include <map>
#include "DependencyObject.h"


typedef int (*PCallback)(void* pUserData, void* pReserved);

struct CallbackData 
{
    CallbackData()
    {
        nType       = 0;
        pfnCallback = 0;
        pUserData   = 0;
        pReserved   = 0;
    }
    
    int         nType;
    PCallback   pfnCallback;
    void*       pUserData;
    void*       pReserved;
};

struct Table
{
	int			 nID;
	CallbackData data;
};

#define MAP_SIZE 10

class CCallbackManager : public CDependencyObject
{
    CCallbackManager();
    virtual ~CCallbackManager();
    
public:
    static CCallbackManager* GetInstance();
    
    int SetCallback(int nType, PCallback pfnCallback, void* pUserData, void* pReserved);
    CallbackData& GetCallbackData(int nType);
    
protected:
    //std::map<int, CallbackData> m_mapCallbacks;
    Table	m_mapCallbacks[MAP_SIZE];
};

#endif
