#ifndef _AVRORARTCTRACE_H_
#define _AVRORARTCTRACE_H_

#include <stdarg.h>
#include <avr/pgmspace.h>

#ifdef __GNUC__
# define AVRORA_PRINT_INLINE __inline__
#else
/* Try the C99 keyword instead. */
# define AVRORA_PRINT_INLINE inline
#endif
volatile uint8_t rtcMonitorVariable[8];

#define AVRORA_RTC_SINGLEWORDINSTRUCTION     1
#define AVRORA_RTC_DOUBLEWORDINSTRUCTION     2
#define AVRORA_RTC_STARTMETHOD               3
#define AVRORA_RTC_ENDMETHOD                 4
#define AVRORA_RTC_JAVAOPCODE                5
#define AVRORA_RTC_PATCHINGBRANCHES_ON       6
#define AVRORA_RTC_PATCHINGBRANCHES_OFF      7
#define AVRORA_RTC_STACKCACHESTATE           8
#define AVRORA_RTC_STACKCACHEVALUETAGS       9
#define AVRORA_RTC_STACKCACHEPINNEDREGISTERS 10
#define AVRORA_RTC_STACKCACHESKIPINSTRUCTION 11
#define AVRORA_RTC_INIT                      42
#define AVRORA_RTC_SETCURRENTINFUSION        43
#define AVRORA_RTC_RUNTIMEMETHODCALL         44
#define AVRORA_RTC_RUNTIMEMETHODCALLRETURN   45
#define AVRORA_RTC_PRINTALLRUNTIMEAOTCALLS   46
#define AVRORA_RTC_PRINTCURRENTAOTCALLSTACK  47
#define AVRORA_RTC_STARTCOUNTINGCALLS        48
#define AVRORA_RTC_STOPCOUNTINGCALLS         49

#define AVRORA_RTC_BEEP                      50
#define AVRORA_RTC_TERMINATEONEXCEPTION      51
#define AVRORA_RTC_EMITPROLOGUE              52

static AVRORA_PRINT_INLINE void avroraRTCTraceSingleWordInstruction(uint16_t opcode)
{
	*((uint16_t *)(rtcMonitorVariable+1)) = opcode;
	rtcMonitorVariable[0] = AVRORA_RTC_SINGLEWORDINSTRUCTION;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceDoubleWordInstruction(uint16_t opcode1, uint16_t opcode2)
{
	*((uint16_t *)(rtcMonitorVariable+1)) = opcode1;
	*((uint16_t *)(rtcMonitorVariable+3)) = opcode2;
	rtcMonitorVariable[0] = AVRORA_RTC_DOUBLEWORDINSTRUCTION;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceStartMethod(uint8_t method_impl_id, uint32_t address)
{
	rtcMonitorVariable[1] = method_impl_id;
	*((uint32_t *)(rtcMonitorVariable+2)) = address;
	rtcMonitorVariable[0] = AVRORA_RTC_STARTMETHOD;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceEndMethod(uint32_t address, uint16_t jvmmethodsize, uint8_t numberofbranchtargets)
{
	*((uint32_t *)(rtcMonitorVariable+1)) = address;
	*((uint16_t *)(rtcMonitorVariable+5)) = jvmmethodsize;
	*((uint8_t *)(rtcMonitorVariable+7)) = numberofbranchtargets;
	rtcMonitorVariable[0] = AVRORA_RTC_ENDMETHOD;
}

static AVRORA_PRINT_INLINE void avroraRTCTraceDarjeelingOpcodeInProgmem(unsigned int pointer)
{
	rtcMonitorVariable[1] = pgm_read_byte_far(pointer + 0);
	rtcMonitorVariable[2] = pgm_read_byte_far(pointer + 1);
	rtcMonitorVariable[3] = pgm_read_byte_far(pointer + 2);
	rtcMonitorVariable[4] = pgm_read_byte_far(pointer + 3);
	rtcMonitorVariable[5] = pgm_read_byte_far(pointer + 4);
	rtcMonitorVariable[0] = AVRORA_RTC_JAVAOPCODE;
}

static AVRORA_PRINT_INLINE void avroraRTCTracePatchingBranchesOn()
{
	rtcMonitorVariable[0] = AVRORA_RTC_PATCHINGBRANCHES_ON;
}

static AVRORA_PRINT_INLINE void avroraRTCTracePatchingBranchesOff()
{
	rtcMonitorVariable[0] = AVRORA_RTC_PATCHINGBRANCHES_OFF;
}
static AVRORA_PRINT_INLINE void avroraRTCTraceStackCacheState(uint8_t *cachestate)
{
	rtcMonitorVariable[1] = ((uint16_t)cachestate) & 0xFF;
	rtcMonitorVariable[2] = (((uint16_t)cachestate) >> 8)& 0xFF;
	rtcMonitorVariable[0] = AVRORA_RTC_STACKCACHESTATE;	
}
static AVRORA_PRINT_INLINE void avroraRTCTraceStackCacheValuetags(uint16_t *cachestate_valuetags)
{
	rtcMonitorVariable[1] = ((uint16_t)cachestate_valuetags) & 0xFF;
	rtcMonitorVariable[2] = (((uint16_t)cachestate_valuetags) >> 8)& 0xFF;
	rtcMonitorVariable[0] = AVRORA_RTC_STACKCACHEVALUETAGS;	
}
static AVRORA_PRINT_INLINE void avroraRTCTraceInit()
{
	rtcMonitorVariable[0] = AVRORA_RTC_INIT;	
}
static AVRORA_PRINT_INLINE void avroraRTCSetCurrentInfusion(uint32_t infusionName)
{
	rtcMonitorVariable[1] = (infusionName) & 0xFF;
	rtcMonitorVariable[2] = ((infusionName) >> 8)& 0xFF;
	rtcMonitorVariable[3] = ((infusionName) >> 16)& 0xFF;
	rtcMonitorVariable[4] = ((infusionName) >> 24)& 0xFF;
	rtcMonitorVariable[0] = AVRORA_RTC_SETCURRENTINFUSION;
}
static AVRORA_PRINT_INLINE void avroraRTCTraceStackCachePinnedRegisters(uint16_t cachestate_pinnedregisters)
{
	rtcMonitorVariable[1] = cachestate_pinnedregisters & 0xFF;
	rtcMonitorVariable[2] = (cachestate_pinnedregisters >> 8) & 0xFF;
	rtcMonitorVariable[0] = AVRORA_RTC_STACKCACHEPINNEDREGISTERS;	
}
static AVRORA_PRINT_INLINE void avroraRTCTraceStackCacheSkipInstruction(uint8_t reason)
{
	rtcMonitorVariable[1] = reason;
	rtcMonitorVariable[0] = AVRORA_RTC_STACKCACHESKIPINSTRUCTION;	
}
static AVRORA_PRINT_INLINE void avroraRTCRuntimeMethodCall(uint32_t infusionName, uint8_t method_impl_id)
{
	rtcMonitorVariable[1] = (infusionName) & 0xFF;
	rtcMonitorVariable[2] = ((infusionName) >> 8)& 0xFF;
	rtcMonitorVariable[3] = ((infusionName) >> 16)& 0xFF;
	rtcMonitorVariable[4] = ((infusionName) >> 24)& 0xFF;
	rtcMonitorVariable[5] = method_impl_id;
	rtcMonitorVariable[0] = AVRORA_RTC_RUNTIMEMETHODCALL;	
}
static AVRORA_PRINT_INLINE void avroraRTCRuntimeMethodCallReturn()
{
	rtcMonitorVariable[0] = AVRORA_RTC_RUNTIMEMETHODCALLRETURN;	
}
static AVRORA_PRINT_INLINE void avroraRTCPrintAllRuntimeAOTCalls()
{
	rtcMonitorVariable[0] = AVRORA_RTC_PRINTALLRUNTIMEAOTCALLS;	
}
static AVRORA_PRINT_INLINE void avroraRTCCurrentAOTCallStack()
{
	rtcMonitorVariable[0] = AVRORA_RTC_PRINTCURRENTAOTCALLSTACK;	
}

static AVRORA_PRINT_INLINE void avroraRTCStartCountingCalls()
{
	rtcMonitorVariable[0] = AVRORA_RTC_STARTCOUNTINGCALLS;	
}
static AVRORA_PRINT_INLINE void avroraRTCStopCountingCalls()
{
	rtcMonitorVariable[0] = AVRORA_RTC_STOPCOUNTINGCALLS;	
}
static AVRORA_PRINT_INLINE void avroraRTCRuntimeBeep(uint8_t number)
{
	rtcMonitorVariable[1] = number;
	rtcMonitorVariable[0] = AVRORA_RTC_BEEP;	
}
static AVRORA_PRINT_INLINE void avroraTerminateOnException(uint16_t type)
{
	rtcMonitorVariable[1] = (type) & 0xFF;
	rtcMonitorVariable[2] = ((type) >> 8)& 0xFF;
	rtcMonitorVariable[0] = AVRORA_RTC_TERMINATEONEXCEPTION;
}
static AVRORA_PRINT_INLINE void avroraRTCTraceEmitPrologue()
{
	rtcMonitorVariable[0] = AVRORA_RTC_EMITPROLOGUE;
}
#endif
