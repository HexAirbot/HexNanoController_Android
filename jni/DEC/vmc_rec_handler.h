#ifndef VMC__REC_HANDLER_H

#define VMC__REC_HANDLER_H






enum  rec_error_id{
	REC_OK=0,
	REC_INIT_ERR,
	REC_ERR,
	REC_START_ERR
		
};


#define INIT_REC_ERROR "uninit or init error!"
#define REC_ERROR "rec_error!"
#define START_REC_THREAD_ERROR "start rec thread error!"


typedef void  (*rec_callback)(int state, char * error );





static char * rec_erro_info[]={
  NULL, 				// 0,
  INIT_REC_ERROR,			// 1
  REC_ERROR, 	   // 2
  START_REC_THREAD_ERROR   // 3
  
};






int start_rec_hander(char * dst_dir, char * filename , bool location_flag, rec_callback callback);
int close_rec_hander(void);




#endif //VMC__REC_HANDLER_H

