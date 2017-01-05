/*
 * java_lang_Thread.c
 * 
 * Copyright (c) 2008-2010 CSIRO, Delft University of Technology.
 * 
 * This file is part of Darjeeling.
 * 
 * Darjeeling is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Darjeeling is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Darjeeling.  If not, see <http://www.gnu.org/licenses/>.
 */
 

#include <stddef.h>

#include "execution.h"
#include "global_id.h"
#include "debug.h"
#include "heap.h"
#include "panic.h"

#include "pointerwidth.h"

// generated by the infuser
#include "jlib_base.h"

// short java.lang.Thread._create()
void java_lang_Thread_short__create()
{
	// create a new thread
	dj_thread *thread = dj_thread_create();

    if(thread == NULL)
    {
    	dj_exec_createAndThrow(BASE_CDEF_java_lang_OutOfMemoryError);
    	return;
    }

	dj_vm_addThread(dj_exec_getVM(), thread);

	// return thread ID as a short
	dj_exec_stackPushShort(thread->id);
}

// void java.lang.Thread._start(short)
void java_lang_Thread_void__start_short()
{
	// pop thread Id and get the corresponding Thread object
	int16_t id = dj_exec_stackPopShort();
	dj_thread * thread = dj_vm_getThreadById(dj_exec_getVM(), id);
	dj_mem_addSafePointer((void**)&thread);

	// create a ResolvedId to represent the method definition we're looking for
	dj_global_id methodDefId;
	methodDefId.infusion = dj_vm_getSystemInfusion(dj_exec_getVM());
	methodDefId.entity_id = BASE_MDEF_void_run;

	// lookup method
	dj_global_id methodImplId = dj_global_id_lookupVirtualMethod(methodDefId, thread->runnable);

	// create a frame for the 'run' function and push it on the thread stack
	dj_frame *frame = dj_frame_create(methodImplId);

	// check that the frame alloc was succesful
	if(frame == NULL)
	{
		dj_exec_createAndThrow(BASE_CDEF_java_lang_StackOverflowError);
	} else
	{

		// push the new frame on the thread's frame stack
		thread->frameStack = frame;

		// copy the runnable object to the first reference local variable ('this') in the
		// new frame
		dj_frame_getLocalReferenceVariables(frame)[0] = VOIDP_TO_REF(thread->runnable);

		// mark new thread eligible for execution
		thread->status = THREADSTATUS_RUNNING;
	}

	dj_mem_removeSafePointer((void**)&thread);
}

// short java.lang.Thread._getStatus(short)
void java_lang_Thread_short__getStatus_short()
{
	int16_t id = dj_exec_stackPopShort();
	dj_thread * thread = dj_vm_getThreadById(dj_exec_getVM(), id);

	if (thread==0)
		dj_exec_stackPushShort(-1);
	else
		dj_exec_stackPushShort(thread->status);
}

// void java.lang.Thread._setRunnable(short, java.lang.Runnable)
void java_lang_Thread_void__setRunnable_short_java_lang_Runnable()
{
	ref_t runnable = dj_exec_stackPopRef();
	int16_t id = dj_exec_stackPopShort();

	dj_thread * thread = dj_vm_getThreadById(dj_exec_getVM(), id);
	thread->runnable = REF_TO_VOIDP(runnable);
}

// void java.lang.Thread.sleep(long)
void java_lang_Thread_void_sleep_long()
{
	int64_t time = dj_exec_stackPopLong();

	dj_thread *thread = dj_exec_getCurrentThread();
	dj_thread_sleep(thread, time);
	dj_exec_breakExecution();
}

// short java.lang.Thread._getCurrentThreadId()
void java_lang_Thread_short__getCurrentThreadId()
{
	dj_exec_stackPushShort(dj_exec_getCurrentThread()->id);
}

// int java.lang.Thread.activeCount()
void java_lang_Thread_int_activeCount()
{
	dj_exec_stackPushInt(dj_vm_countLiveThreads(dj_exec_getVM()));
}

// void java.lang.Thread.yield()
void java_lang_Thread_void_yield()
{
	dj_exec_breakExecution();
}