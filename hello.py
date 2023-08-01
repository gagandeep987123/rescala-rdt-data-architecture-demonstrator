import subprocess, os, psutil,sys


# pid = os.fork()
# if pid == 0:
#    subprocess.run(["Python3","tensecwait.py"])


def is_program_running(program_name):
    """
    Check if a program with the given name is running.
    """
    for proc in psutil.process_iter(['pid', 'name', 'cmdline']):
        try:
            if proc.info['cmdline'] is not None and program_name in proc.info['cmdline']:
                return True
        except (psutil.AccessDenied, psutil.NoSuchProcess):
            pass
    return False


if __name__ == "__main__":
    model_in = sys.argv[1]
    threads = sys.argv[2]
    batch_size = sys.argv[3]
    sgx_flag = sys.argv[4]
    if is_program_running("./call_parent.sh"):
        print("Program is running")
    else:
        print("starting a process")

        cmd = ["setsid", "./call_parent.sh",model_in,threads,batch_size,sgx_flag]
        result = subprocess.Popen(cmd, preexec_fn=os.setsid,#">/dev/null","2>/dev/null","</dev/null"],
            stdout = subprocess.DEVNULL,
            stderr=subprocess.DEVNULL)
        