import gradio as gr
import requests
import json


def chat(msg, history, indexName):
  try:
    response = requests.get(
        "http://localhost:8080/chat",
    )

    collected_message = ""

    for line in response.iter_lines(decode_unicode=True):
        if line:
            content = json.loads(line[5:])["text"]
            collected_message += content
            yield collected_message

    return collected_message
  except requests.exceptions.RequestException as e:
    print(f"Request failed: {e}")
    yield "Error: Could not connect to chat service."
  except json.JSONDecodeError as e:
    print(f"JSON decode error: {e}")
    yield "Error: Could not decode chat service response."


with gr.Blocks(fill_height=True, css_paths="./style.css") as iface:
    with gr.Tab("Chat", scale=55, elem_id="chat_tab"):
        # Add an informational text to display the current index
        current_index_info = gr.Dropdown(
            label="Current Index", 
            value="default",
            interactive=True,
            #choices=load_index_from_server()
        )
        
        # Add event handler to update the global variable when the textbox changes
        def update_current_index(new_index):
            return gr.Dropdown(value=new_index)
            
        load_index_button = gr.Button("Load Index")
        
        chat_interface = gr.ChatInterface(fn=chat, fill_height=True, type="tuples", additional_inputs=[current_index_info])
    
    with gr.Tab("Link Viewer", scale=55):
        with gr.Row():
            link_input = gr.Textbox(label="Enter URL", placeholder="Github repository or any other URL", value="https://github.com/atomwalk12/PPS-22-git-insp")
            options_dropdown = gr.Dropdown(
                choices=["Github", "Plain Text", "Markdown", "Summary"],
                label="Content Format",
                value="Github"
            )
            extension = gr.Textbox(label="Extension", placeholder="Extensions separated by commas", value="scala,md")

        fetch_button = gr.Button("Fetch Content")
        
        # Use a Column with fill_height to contain the TextArea
        with gr.Column(scale=100, elem_id="content_column"):
            content_display = gr.TextArea(
                label="Link Content", 
                interactive=True, 
                scale=100, 
                elem_id="content_display_area",  # Add an ID for CSS targeting
            )
            
        generate_index_button = gr.Button("Generate Index")
        
        with gr.Row():
            status_display = gr.Textbox(label="Status", interactive=False)


iface.launch()
